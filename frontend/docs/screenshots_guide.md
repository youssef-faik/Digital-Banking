## Manual Screenshot Guide

This guide lists the screenshots to capture manually for the project report. Ensure your application is running at `http://localhost:4200`.

Save all screenshots in the `frontend/docs/screenshots/` directory.

---

1.  **Login Page (Initial)**
    *   **Application Path/State:** Navigate to `http://localhost:4200/login`
    *   **Suggested Filename:** `01_01_login_page_initial.png`
    *   **Placeholder:** `![Login Page Initial](screenshots/01_01_login_page_initial.png)`

2.  **Login Page (Incorrect Credentials)**
    *   **Application Path/State:** On the login page, enter `wrong_user` for username and `wrong_pass` for password, then click the login button. Capture the page showing the error message.
    *   **Suggested Filename:** `01_02_login_page_incorrect_credentials.png`
    *   **Placeholder:** `![Login Page Incorrect Credentials](screenshots/01_02_login_page_incorrect_credentials.png)`

3.  **Login Successful (Dashboard)**
    *   **Application Path/State:** Navigate to `http://localhost:4200/login`. Enter the correct demo credentials (`admin_user` / `123456`) and click login. Capture the dashboard page immediately after successful login.
    *   **Suggested Filename:** `01_03_login_successful_dashboard.png`
    *   **Placeholder:** `![Login Successful Dashboard](screenshots/01_03_login_successful_dashboard.png)`

4.  **Dashboard Overview**
    *   **Application Path/State:** You should already be on the dashboard. Capture the full dashboard page.
    *   **Suggested Filename:** `02_01_dashboard_overview.png` (Capture Full Page)
    *   **Placeholder:** `![Dashboard Overview](screenshots/02_01_dashboard_overview.png)`

5.  **Customer List (Initial)**
    *   **Application Path/State:** Navigate to `http://localhost:4200/customers`.
    *   **Suggested Filename:** `04_01_customer_list_initial.png` (Capture Full Page)
    *   **Placeholder:** `![Customer List Initial](screenshots/04_01_customer_list_initial.png)`

6.  **Customer Form (New - Empty)**
    *   **Application Path/State:** On the customer list page, click the button/link to add a new customer. Capture the empty new customer form.
    *   **Suggested Filename:** `04_02_customer_form_new_empty.png`
    *   **Placeholder:** `![Customer Form New Empty](screenshots/04_02_customer_form_new_empty.png)`

7.  **Customer Form (New - Filled)**
    *   **Application Path/State:** On the new customer form, fill in a temporary customer name (e.g., "Temp Customer Test") and email (e.g., "temp_test@example.com"). Capture the form with the details filled in *before* saving.
    *   **Suggested Filename:** `04_03_customer_form_new_filled.png`
    *   **Placeholder:** `![Customer Form New Filled](screenshots/04_03_customer_form_new_filled.png)`

8.  **Customer List (After Creation)**
    *   **Application Path/State:** Save the new customer. You should be redirected to the customer list. Capture the customer list showing the newly created customer.
    *   **Suggested Filename:** `04_04_customer_list_after_creation.png` (Capture Full Page)
    *   **Placeholder:** `![Customer List After Creation](screenshots/04_04_customer_list_after_creation.png)`

9.  **Customer Form (Edit - Prefilled)**
    *   **Application Path/State:** From the customer list, find the customer you just created (e.g., "Temp Customer Test"). Click the "Edit" button/icon for that customer. Capture the customer form prefilled with their details.
    *   **Suggested Filename:** `04_05_customer_form_edit_prefilled.png`
    *   **Placeholder:** `![Customer Form Edit Prefilled](screenshots/04_05_customer_form_edit_prefilled.png)`

10. **Customer Form (Edit - Filled)**
    *   **Application Path/State:** On the edit customer form, change the customer's name (e.g., to "Temp Customer Test Updated"). Capture the form with the updated name *before* saving.
    *   **Suggested Filename:** `04_06_customer_form_edit_filled.png`
    *   **Placeholder:** `![Customer Form Edit Filled](screenshots/04_06_customer_form_edit_filled.png)`

11. **Customer List (After Update)**
    *   **Application Path/State:** Save the updated customer. You should be redirected to the customer list. Capture the customer list showing the customer with the updated name.
    *   **Suggested Filename:** `04_07_customer_list_after_update.png` (Capture Full Page)
    *   **Placeholder:** `![Customer List After Update](screenshots/04_07_customer_list_after_update.png)`

12. **Customer List (After Delete)**
    *   **Application Path/State:** From the customer list, find the updated customer (e.g., "Temp Customer Test Updated"). Click the "Delete" button/icon for that customer and confirm the deletion. Capture the customer list after the customer has been removed.
    *   **Suggested Filename:** `04_08_customer_list_after_delete.png` (Capture Full Page)
    *   **Placeholder:** `![Customer List After Delete](screenshots/04_08_customer_list_after_delete.png)`

---
**Account Management Screenshots**
*(First, ensure you have a customer like "Temp Account Owner" to associate accounts with.)*
---

13. **Account List (Initial)**
    *   **Application Path/State:** Navigate to `http://localhost:4200/accounts`.
    *   **Suggested Filename:** `05_01_account_list_initial.png` (Capture Full Page)
    *   **Placeholder:** `![Account List Initial](screenshots/05_01_account_list_initial.png)`

14. **Account Form (New - Empty)**
    *   **Application Path/State:** On the account list page, click the button/link to add a new account. Capture the empty new account form.
    *   **Suggested Filename:** `05_02_account_form_new_empty.png`
    *   **Placeholder:** `![Account Form New Empty](screenshots/05_02_account_form_new_empty.png)`

15. **Account Form (New - Filled)**
    *   **Application Path/State:** On the new account form:
        *   Select "Current Account" for Account Type.
        *   Enter "1500" for Initial Balance.
        *   If your form requires selecting a customer, select "Temp Account Owner".
        *   Capture the form with details filled *before* saving.
    *   **Suggested Filename:** `05_03_account_form_new_filled.png`
    *   **Placeholder:** `![Account Form New Filled](screenshots/05_03_account_form_new_filled.png)`

16. **Account List (After Creation)**
    *   **Application Path/State:** Save the new account. You should be redirected to the account list. Capture the account list showing the newly created account.
    *   **Suggested Filename:** `05_04_account_list_after_creation.png` (Capture Full Page)
    *   **Placeholder:** `![Account List After Creation](screenshots/05_04_account_list_after_creation.png)`

17. **Account Details View**
    *   **Application Path/State:** From the account list, find the account you just created. Click to view its details. Capture the account details page. Note the Account ID (let's call it `32db6431-095d-4bff-af9d-6172e346d7c2`).
    *   **Suggested Filename:** `05_05_account_details_view.png` (Capture Full Page)
    *   **Placeholder:** `![Account Details View](screenshots/05_05_account_details_view.png)`

---
**Operations Management Screenshots (on `32db6431-095d-4bff-af9d-6172e346d7c2`)**
---

18. **Operation Debit Form (Empty)**
    *   **Application Path/State:** On the `32db6431-095d-4bff-af9d-6172e346d7c2` details page, click "Debit". Capture the empty debit form.
    *   **Suggested Filename:** `06_01_operation_debit_form_empty.png`
    *   **Placeholder:** `![Operation Debit Form Empty](screenshots/06_01_operation_debit_form_empty.png)`

19. **Operation Debit Form (Filled)**
    *   **Application Path/State:** On the debit form, enter "100" for Amount, "Test Debit" for Description. Capture *before* submitting.
    *   **Suggested Filename:** `06_02_operation_debit_form_filled.png`
    *   **Placeholder:** `![Operation Debit Form Filled](screenshots/06_02_operation_debit_form_filled.png)`

20. **Account Details (After Debit)**
    *   **Application Path/State:** Submit debit. Capture `32db6431-095d-4bff-af9d-6172e346d7c2` details page (updated balance, debit in history).
    *   **Suggested Filename:** `06_03_account_details_after_debit.png` (Capture Full Page)
    *   **Placeholder:** `![Account Details After Debit](screenshots/06_03_account_details_after_debit.png)`

21. **Operation Credit Form (Empty)**
    *   **Application Path/State:** On `32db6431-095d-4bff-af9d-6172e346d7c2` details, click "Credit". Capture empty credit form.
    *   **Suggested Filename:** `06_04_operation_credit_form_empty.png`
    *   **Placeholder:** `![Operation Credit Form Empty](screenshots/06_04_operation_credit_form_empty.png)`

22. **Operation Credit Form (Filled)**
    *   **Application Path/State:** On credit form, enter "200" for Amount, "Test Credit" for Description. Capture *before* submitting.
    *   **Suggested Filename:** `06_05_operation_credit_form_filled.png`
    *   **Placeholder:** `![Operation Credit Form Filled](screenshots/06_05_operation_credit_form_filled.png)`

23. **Account Details (After Credit)**
    *   **Application Path/State:** Submit credit. Capture `32db6431-095d-4bff-af9d-6172e346d7c2` details page (updated balance, credit in history).
    *   **Suggested Filename:** `06_06_account_details_after_credit.png` (Capture Full Page)
    *   **Placeholder:** `![Account Details After Credit](screenshots/06_06_account_details_after_credit.png)`

24. **Operation Transfer Form (Empty)**
    *   **Application Path/State:** On `32db6431-095d-4bff-af9d-6172e346d7c2` details, click "Transfer". Capture empty transfer form.
    *   **Suggested Filename:** `06_07_operation_transfer_form_empty.png`
    *   **Placeholder:** `![Operation Transfer Form Empty](screenshots/06_07_operation_transfer_form_empty.png)`

25. **Operation Transfer Form (Filled)**
    *   **Application Path/State:** On transfer form, enter a **valid destination account ID** (e.g., `e2c9c7ab-96f3-4bb4-92bb-d657c6dec059` or another test account's ID), "50" for Amount, "Test Transfer" for Description. Capture *before* submitting.
    *   **Suggested Filename:** `06_08_operation_transfer_form_filled.png`
    *   **Placeholder:** `![Operation Transfer Form Filled](screenshots/06_08_operation_transfer_form_filled.png)`

26. **Account Details (After Transfer)**
    *   **Application Path/State:** Submit transfer. Capture `32db6431-095d-4bff-af9d-6172e346d7c2` details page (updated balance, transfer in history).
    *   **Suggested Filename:** `06_09_account_details_after_transfer.png` (Capture Full Page)
    *   **Placeholder:** `![Account Details After Transfer](screenshots/06_09_account_details_after_transfer.png)`


