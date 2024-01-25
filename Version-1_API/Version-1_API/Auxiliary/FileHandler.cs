using Version_1_API.Model;

namespace Version_1_API.Auxiliary
{
    public class FileHandler
    {

        public static bool WriteBinaryFile(string directory, FileSubmission file) 
        {
            if (file == null) return false;

            if (!Directory.Exists(directory))
            {
                Directory.CreateDirectory(directory);
            }

            try
            {
                // Open the file with automatic resource management
                using (BinaryWriter writer = new BinaryWriter(File.Open(Path.Combine(directory, file.name), FileMode.Create)))
                {
                    byte[] buffer = Convert.FromBase64String(file.content);

                    if (buffer != null)
                    {
                        writer.Write(buffer);

                        return true;
                    }

                    return false;
                }
                // file is automatically disposed

            }
            catch(FormatException ex)
            {
                throw ex;
            }
            catch(IOException ex) {
                throw ex;
            }
            catch(ObjectDisposedException ex) { throw ex; }

        }

        public static bool WriteTextFile(string directory, FileSubmission file)
        {
            if (file == null) return false;

            if (!Directory.Exists(directory)) {
                Directory.CreateDirectory(directory);
            }

            try
            {
                using (StreamWriter writer = new StreamWriter(Path.Combine(directory, file.name)))
                {
                    writer.Write(file.content);

                    return true;
                }
            }
            catch(IOException ex)
            {
                throw ex;
            }
        }

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
