using Microsoft.AspNetCore.Mvc;
using Microsoft.Net.Http.Headers;
using System.Net;
using Version_1_API.Model;
using Version_1_API.Services;

namespace Version_1_API.Controllers
{
    [ApiController]
    [Route("api/base")]
    public class BaseController : Controller
    {
        private BaseSvc _baseSvc;

        public BaseController()
        {
            _baseSvc = BaseSvc.GetInstance();
        }

        [HttpGet("payload")]
        public ObjectResult GetPayload()
        {
            return Ok(_baseSvc.GetPayload());
        }
    }
}
