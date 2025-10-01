import os
import mysql.connector

# This script attempts to connect to the MySQL database using the
# same environment variables as the main Java web application.
# It now includes the required SSL certificate for Aiven.

print("--- Starting Database Connection Test (with SSL) ---")

try:
    # --- 1. Read Credentials from Environment ---
    db_host = os.getenv("DB_HOST")
    db_port = os.getenv("DB_PORT")
    db_name = os.getenv("DB_NAME")
    db_user = os.getenv("DB_USER")
    db_pass = os.getenv("DB_PASSWORD")

    print(f"Attempting to connect with the following details:")
    print(f"  Host: {db_host}")
    print(f"  Port: {db_port}")
    print(f"  Database: {db_name}")
    print(f"  User: {db_user}")

    if not all([db_host, db_port, db_name, db_user, db_pass]):
        raise ValueError("One or more environment variables are missing!")

    # --- 2. Attempt the Connection with SSL Certificate ---
    print("\nConnecting to MySQL database with SSL certificate...")
    
    # This dictionary holds all the connection arguments
    config = {
        'host': db_host,
        'port': db_port,
        'user': db_user,
        'password': db_pass,
        'database': db_name,
        'ssl_ca': 'ca.pem', # Use the downloaded certificate file
        'ssl_verify_cert': True
    }

    cnx = mysql.connector.connect(**config)

    # --- 3. Report Success ---
    print("\n" + "="*40)
    print("   SUCCESSFULLY CONNECTED TO THE DATABASE! ")
    print("="*40 + "\n")
    
    # Optional: Check if the 'admins' table exists
    cursor = cnx.cursor()
    cursor.execute("SHOW TABLES LIKE 'admins';")
    result = cursor.fetchone()
    if result:
        print("Verified that the 'admins' table exists.")
    else:
        print("WARNING: Could not find the 'admins' table in the database.")
        
    cnx.close()
    print("Connection closed.")

except Exception as e:
    # --- 4. Report Failure ---
    print("\n" + "!"*40)
    print("   FAILED TO CONNECT TO THE DATABASE. ")
    print("   The error is: ", e)
    print("!"*40 + "\n")

print("--- Test Finished ---")

