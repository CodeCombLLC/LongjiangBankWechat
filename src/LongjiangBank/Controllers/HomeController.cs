using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNet.Mvc;
using Microsoft.AspNet.Http;
using Microsoft.Data.Entity;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.PlatformAbstractions;
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
            if (file != null)
            {
                production.Picture = file.ReadAllBytes();
                production.ContentType = file.ContentType;
            }
            DB.Productions.Add(production);
            DB.SaveChanges();
            return _Prompt(x =>
            {
                x.Title = "创建成功";
                x.Details = $"商品【{production.Title}】已经成功创建！";
            });
        }

        [AdminRequired]
        [HttpPost]
        public IActionResult DeleteProduction(Guid id)
        {
            var p = DB.Productions.Single(x => x.Id == id);
            var pending = DB.Exchanges.Where(x => x.ProductionId == id && !x.IsDistributed).ToList();
            foreach (var x in pending)
            {
                x.Customer.Coins += p.Cost;
                DB.Exchanges.Remove(x);
            }
            DB.Productions.Remove(p);
            DB.SaveChanges();
            return Content("ok");
        }

        [AdminRequired]
        [HttpGet]
        public IActionResult EditProduction(Guid id)
        {
            var p = DB.Productions.Single(x => x.Id == id);
            return View(p);
        }

        [AdminRequired]
        [HttpPost]
        public IActionResult EditProduction(Guid id, string title, string description, bool isban, long cost, IFormFile file)
        {
            var p = DB.Productions.Single(x => x.Id == id);
            p.Title = title;
            p.IsBan = isban;
            p.Cost = cost;
            p.Description = description;
            if (file != null)
            {
                p.Picture = file.ReadAllBytes();
                p.ContentType = file.ContentType;
            }
            DB.SaveChanges();
            return _Prompt(x =>
            {
                x.Title = "修改成功";
                x.Details = $"商品【{title}】的信息已经保存成功！";
            });
        }

        [AdminRequired]
        public IActionResult Verify()
        {
            return PagedView(DB.Deposits.Where(x => x.Status == DepositStatus.审核中));
        }

        [AdminRequired]
        [HttpPost]
        public IActionResult Accept(string id, long coins)
        {
            var d = DB.Deposits
                .Include(x => x.Customer)
                .Single(x => x.Id == id);
            d.Status = DepositStatus.兑换成功;
            d.VerifyTime = DateTime.Now;
            d.Coins = coins;
            d.Customer.Coins += coins;
            DB.SaveChanges();
            return Content("ok");
        }

        [AdminRequired]
        [HttpPost]
        public IActionResult Decline(string id)
        {
            var d = DB.Deposits.Single(x => x.Id == id);
            DB.Deposits.Remove(d);
            return Content("ok");
        }

        [AdminRequired]
        public IActionResult Exchange(string name, string prcid)
        {
            if (string.IsNullOrEmpty(name) || string.IsNullOrEmpty(prcid))
                return View();
            var cust = DB.Customers.Where(x => x.Name == name && x.PRCID == prcid).FirstOrDefault();
            var exchanges = DB.Exchanges.Include(x => x.Production).Where(x => x.CustomerId == cust.Id && !x.IsDistributed).ToList();
            return View(exchanges);
        }

        [AdminRequired]
        [HttpPost]
        public IActionResult Distribute(Guid id)
        {
            var ex = DB.Exchanges.Include(x => x.Production).Single(x => x.Id == id);
            ex.IsDistributed = true;
            ex.Production.ExchangeCount++;
            ex.DistributeTime = DateTime.Now;
            DB.SaveChanges();
            return Content("ok");
        }

        [AdminRequired]
        public IActionResult Deposit(DepositStatus? status, string number, DateTime? begin, DateTime? end, string prcid, string name, bool? raw)
        {
            IEnumerable<Deposit> query = DB.Deposits;
            if (status.HasValue)
                query = query.Where(x => x.Status == status);
            if (!string.IsNullOrEmpty(number))
                query = query.Where(x => x.Id == number);
            if (begin.HasValue)
                query = query.Where(x => x.SubmitTime >= begin.Value);
            if (end.HasValue)
                query = query.Where(x => x.SubmitTime <= end.Value);
            if (!string.IsNullOrEmpty(name))
                query = query.Where(x => x.Name == name);
            if (!string.IsNullOrEmpty(prcid))
                query = query.Where(x => x.PRCID == prcid);
            query = query.OrderByDescending(x => x.SubmitTime);
            if (raw.HasValue && raw.Value)
            {
                return XlsView(query.ToList(),"deposit.xls", "Excel");
            }
            else
            {
                return PagedView(query);
            }
        }

        [AdminRequired]
        [HttpGet]
        public IActionResult CreateDeposit()
        {
            return View();
        }

        [AdminRequired]
        [HttpPost]
        public IActionResult CreateDeposit(Deposit deposit)
        {
            if (DB.Deposits.Where(x => x.Id == deposit.Id).Count() > 0)
                return _Prompt(x => 
                {
                    x.Title = "提示信息";
                    x.Details = $"系统中已经存在存单【{deposit.Id}】。请勿重复添加！";
                });
            deposit.CustomerId = null;
            deposit.SubmitTime = DateTime.Now;
            deposit.Status = DepositStatus.待兑换;
            DB.Deposits.Add(deposit);
            DB.SaveChanges();
            return _Prompt(x =>
            {
                x.Title = "创建成功";
                x.Details = $"单号【{deposit.Id}】创建成功！";
            });
        }

        [AdminRequired]
        [HttpPost]
        public IActionResult DeleteDeposit(string id)
        {
            var deposit = DB.Deposits.Single(x => x.Id == id);
            DB.Deposits.Remove(deposit);
            DB.SaveChanges();
            return Content("ok");
        }

        [HttpGet]
        public IActionResult Password()
        {
            return View();
        }

        public IActionResult Password(string old, string @new, string confirm, [FromServices]IConfiguration Config,[FromServices]IApplicationEnvironment env)
        {
            if (@new != confirm)
                return _Prompt(x =>
                {
                    x.Title = "修改失败";
                    x.Details = "两次密码输入不一致！";
                    x.StatusCode = 400;
                });

            if (Config["Password"] != old)
                return _Prompt(x =>
                {
                    x.Title = "修改失败";
                    x.Details = "旧密码不正确！";
                    x.StatusCode = 400;
                });

            var str = "{ \"Username\":\"admin\", \"Password\":\"" + @new + "\" }";
            System.IO.File.WriteAllText(System.IO.Path.Combine(env.ApplicationBasePath, "config.json"), str);
            return _Prompt(x =>
            {
                x.Title = "修改成功";
                x.Details = "新密码已生效！";
            });
        }
    }
}
