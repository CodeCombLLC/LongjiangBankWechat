using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace System
{
    public static class StringHelper
    {
        public static string GetMidText(this string self, string left, string right)
        {
            var begin = self.IndexOf(left) + left.Length;
            var end = self.IndexOf(right);
            return self.Substring(begin, end - begin);
        }
    }
}
