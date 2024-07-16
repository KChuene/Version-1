import login_form
import admin_panel

class Application():
    def __init__(self):
        self.login_window = login_form.Login()


if __name__ == "__main__":
    Application()