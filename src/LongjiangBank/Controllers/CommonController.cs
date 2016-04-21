using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Text.RegularExpressions;
using Microsoft.AspNet.Mvc;
using Microsoft.Data.Entity;
using LongjiangBank.Models;

namespace LongjiangBank.Controllers
{
    public class CommonController : BaseController
    {
        public override void Prepare()
        {
            base.Prepare();
            ViewBag.IsCommon = true;
        }
        [HttpGet]
        public IActionResult Index()
        {
            return View();
        }

        [HttpPost]
        public IActionResult Index(string phone, string prcid, string password, string confirm, string name)
        {
            if (DB.Customers.SingleOrDefault(x => x.Id == phone) != null)
                return Prompt(x =>
                {
                    x.Title = "注册失败";
                    x.Details = $"手机号码{phone}已经注册，请检查后重试！";
                });
            var regex = new Regex("1[0-9]{10}");
            if (!regex.IsMatch(phone))
                return Prompt(x =>
                {
                    x.Title = "注册失败";
                    x.Details = "您输入的手机号码不是有效的手机号码，请返回后重试！";
                });
            var cust = new Customer
            {
                Name = name,
                Phone = phone,
                Id = phone,
                Password = password,
                PRCID = prcid,
                Coins = 0
            };
            DB.Customers.Add(cust);
            DB.SaveChanges();
            return Prompt(x =>
            {
                x.Title = "注册成功";
                x.Details = $"手机号码{phone}成功注册，您可以使用该账号兑换积分、兑换礼品了。";
            });
        }

        [HttpGet]
        public IActionResult Coin()
        {
            return View();
        }

        [HttpPost]
        public IActionResult Coin(string number, string phone)
        {
            var uid = phone;

            if (DB.Customers.SingleOrDefault(x => x.Id == phone) == null)
                return Prompt(x =>
                {
                    x.Title = "兑换失败";
                    x.StatusCode = 403;
                    x.Details = "没有找到这个手机号码，请您先注册后兑换！";
                });
            var customer = DB.Customers.Single(x => x.Id == phone);
            var regex = new Regex("[a-zA-Z0-9]{0,}");
            if (string.IsNullOrEmpty(number) || number.Length > 30 || !regex.IsMatch(number))
            {
                return Prompt(x =>
                {
                    x.Title = "兑换失败";
                    x.StatusCode = 403;
                    x.Details = "兑换失败，请填写正确的单号！";
                });
            }

            if (DB.Deposits
                .Where(x => x.Id == number
                    && x.PRCID == customer.PRCID
                    && x.Name == customer.Name
                    && x.Status == DepositStatus.待兑换)
                .Count() > 0)
            {
                var d = DB.Deposits
                    .Include(x => x.Customer)
                    .SingleOrDefault(x => x.Id == number);
                d.CustomerId = customer.Id;
                d.Status = DepositStatus.兑换成功;
                d.VerifyTime = DateTime.Now;
                d.Customer.Coins += d.Coins;
                DB.SaveChanges();
                return Prompt(x =>
                {
                    x.Title = "兑换信息";
                    x.Details = $"您已经成功兑换了【{d.Coins}点积分】，您可以在底部点击积分查询按钮查询总积分！";
                });
            }

            if (DB.Deposits
                .Where(x => x.Id == number)
                .Count() > 0)
                return Prompt(x =>
                {
                    x.Title = "兑换失败";
                    x.Details = "您无权兑换这个存单号或理财号！如有疑问请联系我们的工作人员核实。";
                });

            var deposit = new Deposit
            {
                Id = number,
                Status = DepositStatus.审核中,
                Name = customer.Name,
                PRCID = customer.PRCID,
                SubmitTime = DateTime.Now,
                Coins = 0,
                CustomerId = customer.Id,
                VerifyTime = null,
                Phone = customer.Phone,
                Hint = ""
            };
            DB.Deposits.Add(deposit);
            DB.SaveChanges();

            return Prompt(x =>
            {
                x.Title = "兑换信息";
                x.StatusCode = 403;
                x.Details = "兑换信息已经提交，我们的工作人员会在24小时内处理您的兑换请求！";
            });
        }

        [HttpGet]
        public IActionResult Query()
        {
            return View();
        }

        [HttpPost]
        public IActionResult Query(string phone)
        {
            var cust = DB.Customers.SingleOrDefault(x => x.Id == phone);
            if (cust == null)
                return Prompt(x =>
                {
                    x.Title = "查询失败";
                    x.Details = $"没有找到手机号码为{phone}的相关积分信息！";
                });
            return Prompt(x =>
            {
                x.Title = "查询结果";
                x.Details = $"手机号码为{phone}的账户中有 {cust.Coins} 点积分";
            });
        }

        public IActionResult Shop(string title)
        {
            var ret = DB.Productions.Where(x => !x.IsBan);
            if (!string.IsNullOrEmpty(title))
                ret = ret.Where(x => x.Title.Contains(title) || title.Contains(x.Title));
            ret = ret.OrderByDescending(x => x.Cost);
            return AjaxPagedView(ret, "#lstProducts", 10);
        }

        public IActionResult Production(Guid id)
        {
            var p = DB.Productions.Single(x => x.Id == id && !x.IsBan);
            return View(p);
        }

        public IActionResult ProductionComment(Guid id)
        {
            var p = DB.Productions.Single(x => x.Id == id && !x.IsBan);
            return View(p);
        }

        [HttpPost]
        public IActionResult Exchange(Guid id, string phone, string password)
        {
            var p = DB.Productions.SingleOrDefault(x => x.Id == id && !x.IsBan);
            if (p == null)
                return Prompt(x =>
                {
                    x.Title = "兑换失败";
                    x.Details = "没有找到该商品，或该商品已经下架，请返回积分商城重新选择！";
                });
            var customer = DB.Customers.SingleOrDefault(x => x.Id == phone && x.Password == password);
            if (customer == null)
                return Prompt(x =>
                {
                    x.Title = "兑换失败";
                    x.Details = "手机号码或密码不正确，请返回重试！";
                });
            if (customer.Coins < p.Cost)
                return Prompt(x =>
                {
                    x.Title = "兑换失败";
                    x.Details = $"兑换该商品需要{p.Cost}点积分，您当前只有{customer.Coins}点积分，无法兑换该商品！";
                });
            customer.Coins -= p.Cost;
            DB.Exchanges.Add(new Exchange
            {
                ProductionId = p.Id,
                Time = DateTime.Now,
                IsDistributed = false,
                DistributeTime = null,
                CustomerId = customer.Id
            });
            DB.SaveChanges();
            return Prompt(x =>
            {
                x.Title = "兑换成功";
                x.Details = $"您成功兑换了一件【{p.Title}】，请到银行柜台向工作人员提供您的姓名及身份证号码以兑换该礼品！";
            });
        }

    }
}
