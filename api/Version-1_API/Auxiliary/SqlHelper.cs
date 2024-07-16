using Microsoft.Extensions.ObjectPool;
using Microsoft.IdentityModel.Tokens;
using MySqlConnector;
using System.Data;

namespace Version_1_API.Auxiliary
{
    public class SqlHelper
    {
        private static SqlHelper _instance;
        private MySqlConnection _connection;

        private MySqlConnectionStringBuilder _connStringBuilder = (new MySqlConnectionStringBuilder
        {
            Server = "localhost",
            UserID = "version1.admin",
            Password = "V3rs!0n1.Adm!#",
            Database = "version1"
        });
       
        //"Data Source=(localdb)\\MSSQLLocalDB;Initial Catalog=Accounts;Integrated Security=True;Connect Timeout=30;Encrypt=False;Trust Server Certificate=False;Application Intent=ReadWrite;Multi Subnet Failover=False";

        private SqlHelper() {
            _connection = new MySqlConnection(_connStringBuilder.ConnectionString);
            _connection.Open();
        }

        public static SqlHelper GetInstance()
        {
            if(_instance == null )
            {
                _instance = new SqlHelper();
            }
            return _instance;
        }

        public int Insert(string sql, string[] parameters, params object[] values)
        {
            MySqlCommand cmd = new MySqlCommand(sql, _connection);
            MySqlTransaction transaction = null;

            try
            {
                SetParameters(cmd, parameters, values);

                cmd.Connection.Open();
                cmd.Transaction = _connection.BeginTransaction();

                int changes = 0;
                if (transaction != null)
                {
                    changes = cmd.ExecuteNonQuery();
                    transaction.Commit();
                }

                return changes;
            }
            catch (MySqlException ex)
            {
                RollBack(transaction);
                throw ex;
            }
            catch (IOException ex)
            {
                RollBack(transaction);
                throw ex;
            }
            catch (Exception ex)
            {
                RollBack(transaction);
                throw ex;
            }
            finally { 
                CloseConnection(); 
            }
        }

        public MySqlDataReader Select(string sql, string[] parameters, params object[] values)
        {
            MySqlCommand cmd = new MySqlCommand(sql, _connection);

            try
            {
                SetParameters(cmd, parameters, values);

                OpenConnection();
                MySqlDataReader reader = cmd.ExecuteReader();
                
                return reader;
            }
            catch (Exception ex)
            {
                throw ex;
            }

        }

        private void RollBack(MySqlTransaction transation)
        {
            try
            {
                if (transation != null)
                {
                    transation.Rollback();
                }
            }
            catch(Exception) { /*pass*/ }
        }

        private void SetParameters(MySqlCommand cmd, string[] parameters, object[] values)
        {
            if(parameters.Length != values.Length)
            {
                throw new ArgumentException();
            }

            for(int index = 0; index < parameters.Length; index++)
            {
                cmd.Parameters.AddWithValue(parameters[index], values[index]);
            }
        }

        private void OpenConnection()
        {
            try
            {
                if(_connection != null && _connection.State == ConnectionState.Closed)
                {
                    _connection.Open();
                }
            }
            catch(Exception ex) { }
        }

        public void CloseConnection()
        {
            try
            {
                if (_connection != null && _connection.State == ConnectionState.Open)
                {
                    _connection.Close();
                }
            }
            catch(MySqlException) { /*pass*/ } 
        }
    }
}
