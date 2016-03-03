using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNet.Mvc;
using Microsoft.AspNet.Http;
using Microsoft.Extensions.Configuration;
using LongjiangBank.Models;
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

        [AdminRequired]
        public IActionResult Mall(string title, long? gte, long? lte, string isban)
        {
            IEnumerable<Production> query = DB.Productions;
            if (!string.IsNullOrEmpty(title))
                query = query.Where(x => x.Title.Contains(title) || title.Contains(x.Title));
            if (!string.IsNullOrEmpty(isban))
            {
                if (isban == "true")
                    query = query.Where(x => x.IsBan);
                else
                    query = query.Where(x => !x.IsBan);
            }
            if (gte.HasValue)
                query = query.Where(x => x.Cost >= gte.Value);
            if (lte.HasValue)
                query = query.Where(x => x.Cost <= lte.Value);
            return PagedView(query);
        }

        [AdminRequired]
        [HttpGet]
        public IActionResult CreateProduction()
        {
            return View();
        }

        [AdminRequired]
        [HttpPost]
        public IActionResult CreateProduction(Production production, IFormFile file)
        {
            production.ExchangeCount = 0;
            production.Picture = file.ReadAllBytes();
            production.ContentType = file.ContentType;
            DB.Productions.Add(production);
            DB.SaveChanges();
            return _Prompt(x =>
            {
                x.Title = "创建成功";
                x.Details = $"商品【{production.Title}】已经成功创建！";
            });
        }
    }
}
