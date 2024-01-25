using Microsoft.AspNetCore.Mvc.RazorPages.Infrastructure;
using Microsoft.Data.SqlClient;
using System.Diagnostics.SymbolStore;
using System.Net;
using System.Net.Mail;
using System.Security.Cryptography;
using System.Text;
using Version_1_API.Auxiliary;
using Version_1_API.Model;

namespace Version_1_API.Services
{
    public class AccountManagerSvc
    {
        private List<string> sessions = new List<string>();
        private static AccountManagerSvc _instance;
        private SqlHelper _sqlHelper;
        
        private AccountManagerSvc()
        {
            _sqlHelper = SqlHelper.GetInstance();
        }

        public static AccountManagerSvc GetInstance()
        {
            if(_instance == null)
            {
                _instance = new AccountManagerSvc();
            }
            return _instance;
        }

        public Response AuthenticateAddress(string address, string password)
        {
            string query =
                "SELECT Id FROM Credentials WHERE Address=@address AND PassHash=@password;";

            try
            {
                string[] parameters = new string[] {
                    nameof(address),
                    nameof(password)
                };

                SqlDataReader reader = _sqlHelper.Select(query, parameters, address, password);
                if (reader.HasRows)
                {
                    string token = CreateCode(16);

                    sessions.Add(token);
                    return new Response(
                        (int)EApiExecCode.SUCCESS, "Logged in.", token);
                    // return SendAuthCode(address, CreateAuthCode(address, 4)); // n-char long auth code
                }
                else
                {
                    return new Response(
                        (int)EApiExecCode.FAIL, "Either the password or email is incorrect.", String.Empty);
                }
            }
            catch (Exception ex)
            {

                Console.WriteLine(ex.StackTrace);
                return new Response(
                    (int)EApiExecCode.ERROR, "Unexpected error occured. Try again later.", String.Empty);
            }
            finally
            {
                _sqlHelper.CloseConnection();
            }
        }

        public bool SessionExists(string sessionId)
        {
            return sessions.Contains(sessionId);
        }

        private String CreateCode(int size)
        { 
            int[] start = new int[] { 48, 65, 97 }; // lower bounding unicode decimals
            int[] end = new int[] { 58, 91, 122 }; // upper bounding unicode decimals

            // A pass code of length size
            StringBuilder code = new StringBuilder();
            for(int count = 1; count <= size; count++)
            {
                int unicodeSubset = RandomNumberGenerator.GetInt32(0, start.Length);

                // if unicodeSubset EQ 2 the next char is char from 97 to 122
                code.Append( Convert.ToChar(
                    RandomNumberGenerator.GetInt32(start[unicodeSubset], end[unicodeSubset])));
            }

            return code.ToString();
        }

        private Response SendAuthCode(string recipient, string code)
        {
            // Construct Message

            MailMessage message = new MailMessage(MailServer.User, recipient);
            message.Subject = "Version 1 - Verification Code";
            message.Body = $"Your verification code: <b>{code}</b>";
            message.IsBodyHtml = true;

            try
            {
                // Send authentication code via email
                using (SmtpClient smtpClient = new SmtpClient(MailServer.Host, MailServer.Port))
                {
                    smtpClient.Credentials = new NetworkCredential(MailServer.User, MailServer.Pass);
                    smtpClient.EnableSsl = true;
                    smtpClient.Timeout = 20000;
                    smtpClient.Send(message);

                    return new Response(
                        (int)EApiExecCode.SUCCESS, "Successfully sent authentication code.", true.ToString());
                }
            }
            catch (SmtpFailedRecipientException)
            {
                return new Response(
                    (int)EApiExecCode.ERROR, "Failed to send auth code to specified email address.", false.ToString());
            }
            catch (SmtpException ex)
            {
                Console.WriteLine(ex.StackTrace);
                return new Response(
                    (int)EApiExecCode.ERROR, "Failed to complete sending of auth code.", false.ToString());
            }
            catch(Exception ex)
            {
                Console.WriteLine(ex.StackTrace);
                return new Response(
                    (int)EApiExecCode.ERROR, "Unexpected error sending auth code.", false.ToString());
            }
            finally
            {
                message.Dispose();
            }
        }


    }
}
