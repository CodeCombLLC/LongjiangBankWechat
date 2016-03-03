using System;
using System.IO;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNet.Http;
using Microsoft.AspNet.Mvc;
using LongjiangBank.Models;

namespace LongjiangBank.Controllers
{
    public class ApiController : BaseController
    {
        public IActionResult Coin()
        {
            var reader = new StreamReader(Request.Body);
            var xml = reader.ReadToEnd();
            var fromUsername = xml.GetMidText("<FromUserName><![CDATA[", "]]></FromUserName>");
            var toUsername = xml.GetMidText("<ToUserName><![CDATA[", "]]></ToUserName>");
            var ret = $@"<xml>
<ToUserName><![CDATA[{fromUsername}]]></ToUserName>
<FromUserName><![CDATA[{toUsername}]]></FromUserName>
<CreateTime>{DateTime.Now.ToTimeStamp() / 1000}</CreateTime>
<MsgType><![CDATA[news]]></MsgType>
<ArticleCount>1</ArticleCount>
<Articles>
<item>
<Title>输单号，兑积分，赢豪礼</Title>
<Description><![CDATA[输入在银行的存单号或理财编号，即可兑换积分，浏览积分商城，兑换豪华礼品，还等什么，赶快行动吧！]]></Description>
<PicUrl><![CDATA[http://221.209.110.83:9989/images/coin.png]]></PicUrl>
<Url><![CDATA[http://221.209.110.83:9989/api/coins?uid={fromUsername}]]></Url>
</item>
</Articles>
</xml> ";
            return Content(ret);
        }

        [HttpGet]
        public IActionResult Coins(string uid)
        {
            if (!string.IsNullOrEmpty(uid) && DB.Customers.Where(x => x.Id == uid).Count() == 0)
            {
                var customer = new Customer
                {
                    Id = uid,
                    Coins = 0
                };
                DB.Customers.Add(customer);
                DB.SaveChanges();
            }
            HttpContext.Session.SetString("uid", uid);
            return View();
        }

        [HttpPost]
        public IActionResult Coins(string number, string uid)
        {
            if (string.IsNullOrEmpty(number))
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
                    && x.PRCID == Customer.PRCID 
                    && x.Name == Customer.Name 
                    && x.Status == DepositStatus.待兑换)
                .Count() > 0)
            {
                var deposit = DB.Deposits.SingleOrDefault(x => x.Id == number);
                deposit.CustomerId = Customer.Id;
                deposit.Status = DepositStatus.兑换中;
                deposit.SubmitTime = DateTime.Now;
                DB.SaveChanges();
                return View("Coins2", deposit);
            }

            return Prompt(x => 
            {
                x.Title = "兑换失败";
                x.StatusCode = 403;
                x.Details = "兑换失败，您没有资格兑换本张存单或财务编号！";
            });
        }

        public IActionResult Query()
        {
            var reader = new StreamReader(Request.Body);
            var xml = reader.ReadToEnd();
            var fromUsername = xml.GetMidText("<FromUserName><![CDATA[", "]]></FromUserName>");
            var toUsername = xml.GetMidText("<ToUserName><![CDATA[", "]]></ToUserName>");
            var customer = DB.Customers.SingleOrDefault(x => x.Id == fromUsername);
            if (customer == null)
            {
                return Content($@"<xml>
<ToUserName><![CDATA[{fromUsername}]]></ToUserName>
<FromUserName><![CDATA[{toUsername}]]></FromUserName>
<CreateTime>{DateTime.Now.ToTimeStamp() / 1000}</CreateTime>
<MsgType><![CDATA[text]]></MsgType>
<Content><![CDATA[您当前的积分：0]]></Content>
</xml> ");
            }
            else
            {
                return Content($@"<xml>
<ToUserName><![CDATA[{fromUsername}]]></ToUserName>
<FromUserName><![CDATA[{toUsername}]]></FromUserName>
<CreateTime>{DateTime.Now.ToTimeStamp() / 1000}</CreateTime>
<MsgType><![CDATA[text]]></MsgType>
<Content><![CDATA[{"您当前的积分：" + customer.Coins}]]></Content>
</xml> ");
            }
        }

        public IActionResult Shop()
        {
            var reader = new StreamReader(Request.Body);
            var xml = reader.ReadToEnd();
            var fromUsername = xml.GetMidText("<FromUserName><![CDATA[", "]]></FromUserName>");
            var toUsername = xml.GetMidText("<ToUserName><![CDATA[", "]]></ToUserName>");
            var ret = $@"<xml>
<ToUserName><![CDATA[{fromUsername}]]></ToUserName>
<FromUserName><![CDATA[{toUsername}]]></FromUserName>
<CreateTime>{DateTime.Now.ToTimeStamp() / 1000}</CreateTime>
<MsgType><![CDATA[news]]></MsgType>
<ArticleCount>1</ArticleCount>
<Articles>
<item>
<Title>消费积分，兑换豪礼</Title>
<Description><![CDATA[进入积分商城，兑换精美礼品。]]></Description>
<PicUrl><![CDATA[http://221.209.110.83:9989/images/shop.jpg]]></PicUrl>
<Url><![CDATA[http://221.209.110.83:9989/api/mall?uid={fromUsername}]]></Url>
</item>
</Articles>
</xml> ";
            return Content(ret);
        }

        public IActionResult Mall(string title)
        {
            var ret = DB.Productions.Where(x => !x.IsBan);
            if (!string.IsNullOrEmpty(title))
                ret = ret.Where(x => x.Title.Contains(title) || title.Contains(x.Title));
            ret = ret.OrderByDescending(x => x.Cost);
            return AjaxPagedView(ret, "#lstProducts", 10);
        }
    }
}
