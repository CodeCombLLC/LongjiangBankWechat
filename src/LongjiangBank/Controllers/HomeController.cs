using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNet.Mvc;
using Microsoft.AspNet.Http;
using Microsoft.Extensions.Configuration;
using LongjiangBank.Filters;

namespace LongjiangBank.Controllers
{
    public class HomeController : BaseController
    {
        [AdminRequired]
        public IActionResult Index()
        {
            return View();
        }

        [HttpGet]
        public IActionResult Login()
        {
            return View();
        }

        [HttpPost]
        public IActionResult Login(string username, string password, [FromServices] IConfiguration config)
        {
            if (username == config["Username"] && password == config["Password"])
            {
                HttpContext.Session.SetString("Admin", "true");
                return RedirectToAction("Index", "Home");
            }
            else
            {
                return _Prompt(x => 
                {
                    x.Title = "登录失败";
                    x.Details = "请检查用户名及密码后重新尝试登录操作！";
                });
            }
        }

        [HttpPost]
        public IActionResult Logout()
        {
            HttpContext.Session.Clear();
            return RedirectToAction("Login", "Home");
        }
    }
}
