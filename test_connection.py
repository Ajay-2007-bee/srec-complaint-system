import os
import mysql.connector

# This script attempts to connect to the MySQL database using the
# same environment variables as the main Java web application.
# It will print a clear SUCCESS or FAILURE message to the Render logs.

print("--- Starting Database Connection Test ---")

try:
    # --- 1. Read Credentials from Environment ---
    # It's crucial that these match the keys used in your web service.
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
    # We don't print the password for security reasons.

    if not all([db_host, db_port, db_name, db_user, db_pass]):
        raise ValueError("One or more environment variables are missing!")

    # --- 2. Attempt the Connection ---
    print("\nConnecting to MySQL database...")
    
    cnx = mysql.connector.connect(
        host=db_host,
        port=db_port,
        user=db_user,
        password=db_pass,
        database=db_name,
        ssl_verify_cert=True,
        ssl_ca=None # Aiven's certs are usually trusted by the system
    )

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
