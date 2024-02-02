import login_gui
import c2_gui

class Application():
    def __init__(self):
        self.login_window = login_gui.Login()


if __name__ == "__main__":
    Application()