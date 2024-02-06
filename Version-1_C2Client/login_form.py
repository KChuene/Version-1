import customtkinter as ctk
import httpclient
import admin_panel as c2
import hashlib

from tkinter import messagebox
from PIL import Image, ImageTk


class FormFrame(ctk.CTkFrame):

    def __init__(self, master):
        super.__init__()

        self.rowconfigure(0, weight=1)
        self.rowconfigure(1, weight=1)
        self.rowconfigure(2, weight=1)
        self.columnconfigure(0, weight=1)
        self.columnconfigure(1, weight=1)


class Login:
    def __init__(self):
        ctk.set_appearance_mode("dark")
        ctk.set_default_color_theme("themes/lizard.json")

        self.window = ctk.CTk()
        self.window.title("Version 1 - Login")
        self.window.geometry("600x300")
        self.window.eval("tk::PlaceWindow . Center")
        self.window.resizable(False, False)

        self.window.columnconfigure(0, weight=1)
        self.window.columnconfigure(1, weight=1)
        self.window.rowconfigure(0, weight=1)
        self.window.rowconfigure(1, weight=1)

        self.logo_image = Image.open("images/logo-green.png");
        self.logo = ImageTk.PhotoImage(self.logo_image.resize((52, 52)))
        self.logo_holder = ctk.CTkLabel(self.window, text="  VERSION-1", image=self.logo, font=("Arial", 20, "bold"),
                                        compound=ctk.LEFT)
        self.logo_holder.grid(row=0, column=0, sticky="nw", padx=10, pady=10)

        self.form_frame = ctk.CTkFrame(self.window)

        self.api_url_lbl = ctk.CTkLabel(self.form_frame, text="API Url")
        self.api_url_lbl.grid(row=0, column=0, padx=10, pady=(10, 5))
        self.api_url_txt = ctk.CTkEntry(self.form_frame, width=175, height=35,
                                        placeholder_text="http://api.version1.local")
        self.api_url_txt.grid(row=0, column=1, padx=10, pady=(10, 5))
        self.email_lbl = ctk.CTkLabel(self.form_frame, text="Email")
        self.email_lbl.grid(row=1, column=0, padx=10, pady=(5, 5))
        self.email_txt = ctk.CTkEntry(self.form_frame, width=175, height=35, placeholder_text="version1@gmail.com")
        self.email_txt.grid(row=1, column=1, padx=10, pady=(5, 5))
        self.password_lbl = ctk.CTkLabel(self.form_frame, text="Password")
        self.password_lbl.grid(row=2, column=0, padx=10, pady=(5, 10))
        self.password_txt = ctk.CTkEntry(self.form_frame, show="*", width=175, height=35,
                                         placeholder_text="V3rS!0nOne_On1y")
        self.password_txt.grid(row=2, column=1, padx=10, pady=(5, 10))
        self.login_btn = ctk.CTkButton(self.form_frame, text="Login", command=self.login)
        self.login_btn.grid(row=3, column=0, columnspan=2, pady=(0, 10))

        self.form_frame.grid(row=1, column=0, columnspan=2, sticky="n")

        self.window.mainloop()

    def login(self):
        api_url = self.api_url_txt.get().strip() or "http://api.version1.local"
        email = self.email_txt.get().strip() or "version1@gmail.com"
        password = hashlib.sha256( bytes(self.password_txt.get().strip() or "V3rS!0nOne_On1y", "utf-8") ).hexdigest()

        if not (api_url and email and password):
            messagebox.showerror("Incomplete", "Please complete all fields.")
            return

        api_client = httpclient.HttpClient(api_url)
        authenticated, message = api_client.api_authenticate(email, password)

        if not authenticated:
            messagebox.showinfo("Auth. Failure", message or "Authentication failed.")
        else:
            self.window.destroy()

            session_token = message
            c2.AdminPanel(session_token, api_url)


Login()
