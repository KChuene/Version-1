namespace Version_1_API.Model
{
    public class RequestParameter
    {
        /*
        This class is meant to replace the use of parameters by having parameters published 
        in the body of the request, so as to lower avoid showing the Id in the URL which by speculation could lead to 
        reading commands issued by the attacker through endpoints s.a. RequestCmd() if the URL is modified
         */
        public string Name { get; set; }
        public String Value {  get; set; }

        public bool contains(string name, RequestParameter[] parameters)
        {
            if(parameters == null)
                throw new ArgumentNullException("parameters");

            else if(parameters.Length == 0)
                return false;

            foreach(RequestParameter parameter in parameters)
            {
                if (parameter.Name == name)
                {
                    return true;
                }
            }

            return false;
        }

        public static String get(string name, RequestParameter[] parameters)
        {
            if (parameters == null) 
                throw new ArgumentNullException("parameters");

            foreach(RequestParameter param in parameters) { 
                if(param.Name == name)
                {
                    return param.Value;
                }
            }

            throw new KeyNotFoundException();
        }
    }
}
