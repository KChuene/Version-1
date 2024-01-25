using Microsoft.Data.SqlClient;
using Microsoft.Extensions.ObjectPool;
using Microsoft.IdentityModel.Tokens;
using System.Data;

namespace Version_1_API.Auxiliary
{
    public class SqlHelper
    {
        private static SqlHelper _instance;
        private SqlConnection _connection;
        private String _connectionString =
            "Data Source=(localdb)\\MSSQLLocalDB;Initial Catalog=Accounts;Integrated Security=True;Connect Timeout=30;Encrypt=False;Trust Server Certificate=False;Application Intent=ReadWrite;Multi Subnet Failover=False";

        private SqlHelper() {
            _connection = new SqlConnection(_connectionString);
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
            SqlCommand cmd = new SqlCommand(sql, _connection);
            SqlTransaction transaction = null;

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
            catch (SqlException ex)
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

        public SqlDataReader Select(string sql, string[] parameters, params object[] values)
        {
            SqlCommand cmd = new SqlCommand(sql, _connection);

            try
            {
                SetParameters(cmd, parameters, values);

                OpenConnection();
                SqlDataReader reader = cmd.ExecuteReader();
                
                return reader;
            }
            catch (Exception ex)
            {
                throw ex;
            }

        }

        private void RollBack(SqlTransaction transation)
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

        private void SetParameters(SqlCommand cmd, string[] parameters, object[] values)
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
            catch(SqlException) { /*pass*/ } 
        }
    }
}
