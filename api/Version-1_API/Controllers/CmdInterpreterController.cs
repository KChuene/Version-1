using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore.Update;
using Microsoft.IdentityModel.Tokens;
using Version_1_API.Model;
using Version_1_API.Services;

namespace Version_1_API.Controllers
{
    [ApiController]
    [Route("/api/cmdInterpreter")]
    public class CmdInterpreterController : Controller
    {
        private CmdInterpreterSvc _cmdInterpreterSvc;

        public CmdInterpreterController()
        {
            _cmdInterpreterSvc = CmdInterpreterSvc.GetInstance();
        }

        [HttpPost("connection")]
        public ObjectResult GetConnectionID(RequestParameter clientName)
        {
            if (clientName.Value.IsNullOrEmpty())
            {
                return BadRequest(new Response(
                    (int)EApiExecCode.ERROR, "Malformed request for session ID.", String.Empty));
            }

            return Ok(_cmdInterpreterSvc.CreateID(10, clientName.Value));
        }

        [HttpPost("listeners")]
        public ObjectResult GetListeners(RequestParameter sessionToken)
        {
            AccountManagerSvc accountManagerSvc = AccountManagerSvc.GetInstance();


            if (!accountManagerSvc.SessionExists(sessionToken.Value))
            {
                return BadRequest(new Response((int)EApiExecCode.FAIL, "Not authenticated.", String.Empty));
            }

            return Ok(_cmdInterpreterSvc.GetListeners());
        }

        // Submit command
        [HttpPost("submit")]
        public ObjectResult SubmitCmd(Command command)
        {
            if(command.targetId.IsNullOrEmpty() || command.cmdString.IsNullOrEmpty())
            {
                return BadRequest(new CmdResult(command.targetId, "Invalid command."));
            }

            AccountManagerSvc accountManagerSvc = AccountManagerSvc.GetInstance();
            if(!accountManagerSvc.SessionExists(command.issuerId))
            {
                return BadRequest(new CmdResult(command.targetId, "Not authenticated."));
            }

            return Ok(_cmdInterpreterSvc.SubmitCmd(command));
        }

        // Request command
        [HttpPost("request")]
        public ObjectResult RequestCmd(RequestParameter clientId)
        {
            if(clientId == null || clientId.Value.IsNullOrEmpty())
            {
                return Ok(new Command()
                {
                    issuerId = String.Empty, targetId = String.Empty, cmdString = "__init__"
                });;
            }
            return Ok(_cmdInterpreterSvc.RequestCmd(clientId.Value));
        }

        // Submit command result
        [HttpPost("result")]
        public void SubmitCmdResult(CmdResult cmdResult) 
        {
            _cmdInterpreterSvc.SubmitCmdResult(cmdResult);
        }
    }
}
