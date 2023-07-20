import random
import string
from cryptography.fernet import Fernet
import sqlite3
import tkinter as tk
from tkinter import messagebox


class PasswordManager:
    def __init__(self, key):
        self.key = key
        self.passwords = {}

    def store_password(self, account, password):
        cipher_suite = Fernet(self.key)
        encrypted_password = cipher_suite.encrypt(password.encode())
        self.passwords[account] = encrypted_password

        # Store the encrypted password in the database
        conn = sqlite3.connect('passwords.db')
        c = conn.cursor()
        c.execute("INSERT INTO passwords VALUES (?, ?)", (account, encrypted_password))
        conn.commit()
        conn.close()

    def retrieve_password(self, account):
        cipher_suite = Fernet(self.key)
        encrypted_password = self.passwords.get(account)
        if encrypted_password:
            decrypted_password = cipher_suite.decrypt(encrypted_password).decode()
            return decrypted_password
        else:
            # Retrieve the encrypted password from the database
            conn = sqlite3.connect('passwords.db')
            c = conn.cursor()
            c.execute("SELECT password FROM passwords WHERE account=?", (account,))
            result = c.fetchone()
            conn.close()

            if result:
                try:
                    decrypted_password = cipher_suite.decrypt(result[0]).decode()
                    return decrypted_password
                except Exception:
                    return None
            else:
                return None

    def generate_password(self, length=12):
        characters = string.ascii_letters + string.digits + string.punctuation
        password = ''.join(random.choice(characters) for _ in range(length))
        return password


def open_store_password_window():
    store_password_window = tk.Toplevel(main_window)
    store_password_window.title("Store Password")

    def store_password():
        account = account_entry.get()
        password = password_entry.get()

        if not account:
            messagebox.showerror("Error", "Please enter an account.")
        elif not password:
            messagebox.showerror("Error", "Please enter a password.")
        else:
            password_manager.store_password(account, password)
            messagebox.showinfo("Success", "Password stored successfully!")

    account_label = tk.Label(store_password_window, text="Account Name:")
    account_entry = tk.Entry(store_password_window, width=30)
    password_label = tk.Label(store_password_window, text="Password:")
    password_entry = tk.Entry(store_password_window, show="*", width=30)
    store_button = tk.Button(store_password_window, text="Store Password", command=store_password)

    account_label.grid(row=0, column=0, padx=10, pady=10)
    account_entry.grid(row=0, column=1, padx=10, pady=10)
    password_label.grid(row=1, column=0, padx=10, pady=10)
    password_entry.grid(row=1, column=1, padx=10, pady=10)
    store_button.grid(row=2, columnspan=2, padx=10, pady=10)


def open_retrieve_password_window():
    retrieve_password_window = tk.Toplevel(main_window)
    retrieve_password_window.title("Retrieve Password")

    def retrieve_password():
        account = account_entry.get()
        if not account:
            messagebox.showerror("Error", "Please enter an account.")
        else:
            password = password_manager.retrieve_password(account)
            if password is None:
                messagebox.showerror("Error", "Password not found for the specified account.")
            else:
                messagebox.showinfo("Password", f"Password for '{account}': {password}")

    account_label = tk.Label(retrieve_password_window, text="Account Name:")
    account_entry = tk.Entry(retrieve_password_window, width=30)
    retrieve_button = tk.Button(retrieve_password_window, text="Retrieve Password", command=retrieve_password)

    account_label.grid(row=0, column=0, padx=10, pady=10)
    account_entry.grid(row=0, column=1, padx=10, pady=10)
    retrieve_button.grid(row=1, columnspan=2, padx=10, pady=10)


def open_generate_password_window():
    generate_password_window = tk.Toplevel(main_window)
    generate_password_window.title("Generate Password")

    def generate_password():
        password_length_input = password_length_entry.get()
        if password_length_input and not password_length_input.isdigit():
            messagebox.showerror("Error", "Please enter a numeric value for password length.")
            return
        password_length = int(password_length_input) if password_length_input else 12
        password = password_manager.generate_password(password_length)
        messagebox.showinfo("Generated Password", password)

    password_length_label = tk.Label(generate_password_window, text="Password Length:")
    password_length_entry = tk.Entry(generate_password_window, width=30)
    password_length_entry.insert(0, "default 12")
    password_length_entry.config(fg="gray")
    password_length_entry.bind("<FocusIn>", lambda event: clear_placeholder(event, password_length_entry))
    password_length_entry.bind("<FocusOut>", lambda event: restore_placeholder(event, password_length_entry))
    generate_button = tk.Button(generate_password_window, text="Generate Password", command=generate_password)

    password_length_label.grid(row=0, column=0, padx=10, pady=10)
    password_length_entry.grid(row=0, column=1, padx=10, pady=10)
    generate_button.grid(row=1, columnspan=2, padx=10, pady=10)

def clear_placeholder(event, entry):
    if entry.get() == "default 12":
        entry.delete(0, tk.END)
        entry.config(fg="white")

def restore_placeholder(event, entry):
    if entry.get() == "":
        entry.insert(0, "default 12")
        entry.config(fg="gray")


key = Fernet.generate_key()
password_manager = PasswordManager(key)

# Create a database table to store the passwords
conn = sqlite3.connect('passwords.db')
c = conn.cursor()
c.execute("CREATE TABLE IF NOT EXISTS passwords (account TEXT, password TEXT)")
conn.commit()
conn.close()

# Create main window
main_window = tk.Tk()
main_window.title("Password Manager")

# Create and configure widgets
app_name_label = tk.Label(main_window, text="Password Manager", font=("Arial", 16, "bold"))
store_button = tk.Button(main_window, text="Store Password", command=open_store_password_window)
retrieve_button = tk.Button(main_window, text="Retrieve Password", command=open_retrieve_password_window)
generate_button = tk.Button(main_window, text="Generate Password", command=open_generate_password_window)

# Pack layout
app_name_label.pack(pady=10)
store_button.pack(pady=10)
retrieve_button.pack(pady=10)
generate_button.pack(pady=10)

main_window.mainloop()
