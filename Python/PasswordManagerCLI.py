import random
import string
from cryptography.fernet import Fernet
import sqlite3

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
                decrypted_password = cipher_suite.decrypt(result[0]).decode()
                return decrypted_password
            else:
                return "Password not found."
    
    def generate_password(self, length=12):
        characters = string.ascii_letters + string.digits + string.punctuation
        password = ''.join(random.choice(characters) for _ in range(length))
        return password


def main():
    key = Fernet.generate_key()
    password_manager = PasswordManager(key)
    
    # Create a database table to store the passwords
    conn = sqlite3.connect('passwords.db')
    c = conn.cursor()
    c.execute("CREATE TABLE IF NOT EXISTS passwords (account TEXT, password TEXT)")
    conn.commit()
    conn.close()
    
    while True:
        print("------ Password Manager ------")
        print("1. Store Password")
        print("2. Retrieve Password")
        print("3. Generate Password")
        print("4. Exit")
        choice = input("Enter your choice: ")
        
        if choice == "1":
            account = input("Enter account name: ")
            password = input("Enter password: ")
            password_manager.store_password(account, password)
            print("Password stored successfully!")
        elif choice == "2":
            account = input("Enter account name: ")
            password = password_manager.retrieve_password(account)
            print("Password:", password)
        elif choice == "3":
            password_length_input = input("Enter password length (default: 12): ")
            password_length = int(password_length_input) if password_length_input else 12
            password = password_manager.generate_password(password_length)
            print("Generated Password:", password)
        elif choice == "4":
            print("Thank you for using the Password Manager!")
            break
        else:
            print("Invalid choice. Please try again.")
        print()
        
if __name__ == "__main__":
    main()
