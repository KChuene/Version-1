import tkinter as tk
import customtkinter as ctk
import webbrowser

from PIL import Image, ImageTk

import httpclient


class ActionsFrame(ctk.CTkFrame):
    def __int__(self, master):
        super.__init__()

        self.columnconfigure(0, weight=1)
        self.columnconfigure(1, weight=1)
        self.rowconfigure(0, weight=1)
        self.rowconfigure(1, weight=1)
        self.rowconfigure(2, weight=1)

class TargetsFrame(ctk.CTkScrollableFrame):
    def __int__(self, master):
        super.__init__()

        self.columnconfigure(0, weight=1)
        self.columnconfigure(1, weight=1)
        self.columnconfigure(2, weight=1)

class Application:

    def __init__(self):
        ctk.set_appearance_mode("dark")
        ctk.set_default_color_theme("themes/lizard.json")

        self.window = ctk.CTk()
        self.window.title("Version 1 - Command and Control")
        self.window.geometry("600x300")
        self.window.resizable(False, False)
        self.window.config(bg="#292b2f")

        self.modules = [
            "MediaWriter",
            "Exfiltrator"
        ]

        self.window.columnconfigure(0, weight=1)
        self.window.columnconfigure(1, weight=1)
        self.window.rowconfigure(0, weight=2)
        self.window.rowconfigure(1, weight=1)
        self.window.rowconfigure(2, weight=1)

        self.image = Image.open("images/logo-green.png")
        self.logo = ImageTk.PhotoImage(self.image.resize((52, 52)))
        self.logo_holder = ctk.CTkLabel(self.window, text="", image=self.logo)
        self.logo_holder.grid(row=0, column=0, padx=(20, 0), sticky="w")
        self.heading = ctk.CTkLabel(self.window, text="VERSION-1", font=("Arial", 25))
        self.heading.grid(row=0, column=1, sticky="w")

        self.targets_frame = TargetsFrame(self.window, orientation="vertical")
        self.targets_frame.grid(row=1, column=0)

        self.target_id = ctk.CTkCheckBox(self.targets_frame, text="target_id")
        self.target_id.grid(row=0, column=0, padx=10, pady=10)
        self.loginuser = ctk.CTkLabel(self.targets_frame, text="loggedin_user")
        self.loginuser.grid(row=0, column=1, padx=10, pady=10)

        self.actions_frame = ActionsFrame(self.window)
        self.actions_frame.grid(row=1, column=1)

        self.selected_module = ctk.StringVar()
        self.selected_module.set("MediaWriter")
        self.modules_dropdown = ctk.CTkOptionMenu(self.actions_frame, values=self.modules)
        self.modules_dropdown.grid(row=0, column=0, columnspan=2, padx=5, pady=5, sticky="w")

        self.stop_btn = ctk.CTkButton(self.actions_frame, text="Stop")
        self.stop_btn.grid(row=1, column=0, padx=5, pady=5)
        self.start_btn = ctk.CTkButton(self.actions_frame, text="Start")
        self.start_btn.grid(row=1, column=1, padx=5, pady=5)
        self.shell_btn = ctk.CTkButton(self.actions_frame, text="Shell")
        self.shell_btn.grid(row=2, column=0, padx=5, pady=5)

        self.credits = ctk.CTkLabel(self.window, text="Command icons created by Freepik - Flaticon")
        self.credits.bind("<Button>", lambda action: self.visit_credits("https://www.flaticon.com/free-icons/command"))
        self.credits.grid(row=2, column=0, columnspan=2)

        self.window.bind("<Visibility>", self.display_listeners(self.targets_frame))
        self.window.mainloop()

    def display_listeners(self, master):
        #listners = httpclient.api_get_listeners("")
        self.clear_widgets(master)
        listners = [
            {'targetid':'AawDaWDsdsefse', 'user':'pk111'},
            {'targetid':'AdhwASCVEwadaw', 'user':'kali'},
            {'targetid': 'AawDaWDsdsefse', 'user': 'pk111'},
            {'targetid': 'AawDaWDsdsefse', 'user': 'pk111'},
            {'targetid': 'AawDaWDsdsefse', 'user': 'pk111'},
            {'targetid': 'AawDaWDsdsefse', 'user': 'pk111'},
            {'targetid': 'AawDaWDsdsefse', 'user': 'pk111'},
            {'targetid': 'AawDaWDsdsefse', 'user': 'pk111'},
            {'targetid': 'AawDaWDsdsefse', 'user': 'pk111'}
        ]

        row_index = 0
        for listener in listners:
            target_id = ctk.CTkCheckBox(master, text=listener['targetid'])
            target_id.grid(row=row_index, column=0, padx=10, pady=10)
            target_user = ctk.CTkLabel(master, text=listener['user'])
            target_user.grid(row=row_index, column=1, padx=10, pady=10)

            row_index += 1

    def clear_widgets(self, master):
        for widget in master.winfo_children():
            widget.destroy()

    def visit_credits(self, url):
        webbrowser.open(url)

Application()

