namespace Version_1_API.Model
{
    public class Response
    {

        public int code { get; set; }
        public String message { get; set; }
        public String data { get; set; }

        public Response(int code, String message, String data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

    }
}
