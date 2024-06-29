using Version_1_API.Model;

namespace Version_1_API.Auxiliary
{
    public class FileHandler
    {
        public static byte[] ReadBinaryFile(string path)
        {
            if(!File.Exists(path))
            {
                return null;
            }

            try
            {
                return File.ReadAllBytes(path);
            }
            catch(Exception) { return null; }
        }
    }
}
