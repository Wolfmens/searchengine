import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.services.ReturnLemmas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LemmaExample {

    public static void main(String[] args) throws Exception {
//        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
//        List<String> wordBaseForms =
//                luceneMorphology.getNormalForms("леса");
//
//        ReturnLemmas returnLemmas = new ReturnLemmas();
//        String text = "\n" +
//                "<!DOCTYPE html>\n" +
//                "\t<html>\n" +
//                "\t<head>\n" +
//                "<title>Интернет-магазин PlayBack.ru</title>\n" +
//                "<meta name=\"description\" content=\"Продажа по доступным ценам. PlayBack.ru - Интернет-Магазин - Большой выбор смартфонов, планшетов, носимой электроники по низким ценам, отличный сервис, гарантии производителя\">\n" +
//                "<meta name=\"keywords\" content=\"купить, цена, описание, интернет-магазин, интернет, магазин, продажа, смартфоны\">\n" +
//                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
//                "<meta http-equiv=\"Last-Modified\" content=\"Sat, 30 Oct 2021 15:19:33 GMT\">\n" +
//                "<link rel=\"shortcut icon\" href=\"/favicon.ico\">\n" +
//                "<link rel=\"apple-touch-icon\" href=\"/logo_apple.png\">\n" +
//                "<link rel=\"StyleSheet\" href=\"/include_new/styles.css\" type=\"text/css\" media=\"all\">\n" +
//                "\t<link rel=\"stylesheet\" href=\"/include_new/jquery-ui.css\" />\n" +
//                "\t<script type=\"text/javascript\" src=\"https://gc.kis.v2.scr.kaspersky-labs.com/FD126C42-EBFA-4E12-B309-BB3FDD723AC1/main.js?attr=h3QzY239nzvG0QZMbuh89d-60Oq8PX7_u2J_1zMSJp6IouJpLMO0MmuIDKGXgITu\" charset=\"UTF-8\"></script><link rel=\"stylesheet\" crossorigin=\"anonymous\" href=\"https://gc.kis.v2.scr.kaspersky-labs.com/E3E8934C-235A-4B0E-825A-35A08381A191/abn/main.css?attr=aHR0cHM6Ly93d3cucGxheWJhY2sucnUv\"/><script src=\"https://code.jquery.com/jquery-1.8.3.js\"></script>\n" +
//                "\t<script src=\"https://code.jquery.com/ui/1.10.0/jquery-ui.js\"></script>\n" +
//                "\t<script src=\"/jscripts/jquery.inputmask.js\" type=\"text/javascript\"></script>\n" +
//                "\t<script src=\"/jscripts/jquery.inputmask.extensions.js\" type=\"text/javascript\"></script>\n" +
//                "\t<script src=\"/jscripts/jquery.inputmask.numeric.extensions.js\" type=\"text/javascript\"></script>\n" +
//                "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"/fancybox/jquery.fancybox-1.3.4.css\" media=\"screen\" />\n" +
//                "<script type=\"text/javascript\" src=\"/fancybox/jquery.mousewheel-3.0.4.pack.js\"></script>\n" +
//                "\t<script type=\"text/javascript\" src=\"/fancybox/jquery.fancybox-1.3.4.js\"></script>\n" +
//                "\t<script type=\"text/javascript\" src=\"/include_new/playback.js\"></script>\n" +
//                "\t<script>\n" +
//                "  $( function() {\n" +
//                "    $( \"#accordion\" ).accordion({\n" +
//                "      heightStyle: \"content\",\n" +
//                "\t  collapsible: true,\n" +
//                "\t  active : false,\n" +
//                "\t  activate: function( event, ui ) {\n" +
//                "         if ($(ui.newHeader).offset() != null) {\n" +
//                "        ui.newHeader,\n" +
//                "        $(\"html, body\").animate({scrollTop: ($(ui.newHeader).offset().top)}, 500);\n" +
//                "      }\n" +
//                "    }\n" +
//                "    });\n" +
//                "\t} );\n" +
//                "\t$( function() {\n" +
//                "    var icons = {\n" +
//                "      header: \"ui-icon-circle-arrow-e\",\n" +
//                "      activeHeader: \"ui-icon-circle-arrow-s\"\n" +
//                "    };\n" +
//                "    $( \"#accordion\" ).accordion({\n" +
//                "      icons: icons\n" +
//                "    });\n" +
//                "    $( \"#toggle\" ).button().on( \"click\", function() {\n" +
//                "      if ( $( \"#accordion\" ).accordion( \"option\", \"icons\" ) ) {\n" +
//                "        $( \"#accordion\" ).accordion( \"option\", \"icons\", null );\n" +
//                "      } else {\n" +
//                "        $( \"#accordion\" ).accordion( \"option\", \"icons\", icons );\n" +
//                "      }\n" +
//                "    });\n" +
//                "  } );\n" +
//                "  </script>\n" +
//                "  <script type=\"text/javascript\">\n" +
//                "  $(function() {\n" +
//                " \n" +
//                "$(window).scroll(function() {\n" +
//                " \n" +
//                "if($(this).scrollTop() != 0) {\n" +
//                " \n" +
//                "$('#toTop').fadeIn();\n" +
//                " \n" +
//                "} else {\n" +
//                " \n" +
//                "$('#toTop').fadeOut();\n" +
//                " \n" +
//                "}\n" +
//                " \n" +
//                "});\n" +
//                " \n" +
//                "$('#toTop').click(function() {\n" +
//                " \n" +
//                "$('body,html').animate({scrollTop:0},800);\n" +
//                " \n" +
//                "});\n" +
//                " \n" +
//                "});\n" +
//                " \n" +
//                "</script>\n" +
//                "</head>\n" +
//                "<body class=\"body_undertop\" topmargin=\"0\" leftmargin=\"0\" bottommargin=\"0\" rightmargin=\"0\" align=\"center\">\n" +
//                "\n" +
//                "<table class=\"table1\" style=\"box-shadow:0px 0px 32px #595959; margin:5px auto; \" bgcolor=\"#ffffff\" width=\"1024\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\">\n" +
//                "  <tr>\n" +
//                "   <td colspan=\"3\" width=\"1024\">\n" +
//                "  <table width=\"100%\" border=\"0\" height=\"110px\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-top: 0px; margin-bottom: 0px;\">\n" +
//                "  <tr>\n" +
//                "    <td width=\"365px\" rowspan=\"2\" align=\"left\">\n" +
//                "\t\t<table width=\"250px\" align=left><tr><td width=\"60px\" height=\"60px\"><img onClick=\"document.location='http://www.playback.ru';return false\" src=\"/img_new/lolo.png\" class=\"logotip\" alt=\"Playback.ru - фотоаппараты, видеокамеры и аксессуары к ним\" title=\"Playback.ru - фотоаппараты, видеокамеры и аксессуары к ним\"> </td><td valign=\"center\" align=\"left\"><a class=\"tele_span\" href=\"/\"><span class=\"tele_span_playback\">PlayBack.ru</span></a><br><span style=\"cursor: pointer;\" onClick=\"document.location='/waytoplayback.html';return false\" class=\"getcallback2\">5 минут от метро ВДНХ</span></td></tr>\n" +
//                "\t\t</table>\n" +
//                "\t</td>\n" +
//                "\t<td width=\"3px\" rowspan=\"2\" align=\"center\">&nbsp;\n" +
//                "    </td>\n" +
//                "    <td width=\"290px\" rowspan=\"2\">\n" +
//                "\t\t<table width=\"215px\" align=center><tr><td valign=\"center\" align=\"center\"><span class=\"tele_span\"><nobr><a href=\"tel:+74951437771\">8(495)143-77-71</a></nobr></span><span class=\"grrafik\"><nobr><br>пн-пт: c 11 до 20<br>сб-вс: с 11 до 18</nobr></span></td></tr>\n" +
//                "\t\t</table>\n" +
//                "    </td>\n" +
//                "    <td width=\"3px\"  align=\"center\" rowspan=\"2\">&nbsp;\n" +
//                "    </td>\n" +
//                "    <td width=\"185px\">\n" +
//                "\t\t<table width=\"175px\" align=center><tr><td valign=\"center\" align=\"center\"><span class=\"blocknamezpom\" style=\"cursor: pointer;\" onClick=\"document.location='/tell_about_the_problem.html';return false\" >Возникла проблема?<br>Напишите нам!</span></td></tr>\n" +
//                "\t\t</table>\n" +
//                "    <span class=\"tele_span\"></span>\n" +
//                "   \n" +
//                "    </td>\n" +
//                "    <td width=\"3px\" align=\"center\">&nbsp;\n" +
//                "    </td>\n" +
//                "\t<td width=\"179px\">\n" +
//                "\t<table  width=\"175px\" align=center><tr><td width=\"53px\" height=\"50px\" rowspan=\"2\" align=\"left\"><a href=\"/basket.html\"><img src=\"/img_new/basket.png\" width=\"49px\" border=0></a></td><td valign=\"bottom\" align=\"left\" height=\"25px\"><a class=\"tele_span2\" href=\"/basket.html\">Корзина</a><br><span class=\"take_me_call\"></span></td></tr>\n" +
//                "\t<tr>\n" +
//                "\t            <td height=\"10px\" align=\"right\" valign=\"top\"><span class=\"basket_inc_label\" id=\"sosotoyaniekorziny\">пуста</span></td>\n" +
//                "\t</tr></table>\n" +
//                "\t</td>\n" +
//                "\t</tr>\n" +
//                "\t<tr>\n" +
//                "    <td colspan=\"3\" style=\"text-align: right;\">\n" +
//                "\t<form action=\"/search.php\" method=\"get\" class=\"izkat\">\n" +
//                "  <input type=\"search\" name=\"search_string\" placeholder=\"поиск\" class=\"ssstring\"/>\n" +
//                "  <input type=\"submit\" name=\"\" value=\"Искать\" class=\"iskat\"/>\n" +
//                "</form></td>\n" +
//                "   </tr>\n" +
//                "\t</table>\n" +
//                "\t</td>\n" +
//                "\t<!---<tr> \n" +
//                "\t<td colspan=\"3\" style=\"color: #2556A3; font:17px Roboto-Regular,Helvetica,sans-serif; text-align: center; height: 35px;vertical-align: middle;padding-bottom:10px;\">\n" +
//                "\t\t<b>Уважаемые покупатели! Наш график работы в праздничные дни: 9 мая - выходной, остальные дни - по стандартному графику.</b>\n" +
//                "\t</td>\n" +
//                "  </tr>\n" +
//                "    ---->\n" +
//                "  <tr>\n" +
//                "    <td colspan=\"3\" style=\"text-align: center;\">\n" +
//                "\t\n" +
//                "\t\n" +
//                "\t\n" +
//                "\t\n" +
//                "\t\n" +
//                "\t<nav>\n" +
//                "  <ul class=\"topmenu\">\n" +
//                "    <li><a href=\"\" class=\"active\" onclick=\"return false;\"><img src=\"/img/imglist.png\" height=\"9px\"> Каталог<span class=\"fa fa-angle-down\"></span></a>\n" +
//                "      <ul class=\"submenu\">\n" +
//                "<li><a href=\"/catalog/1622.html\">Защитные стекла для смартфонов Apple</a></li><li><a href=\"/catalog/1626.html\">Чехлы для смартфонов Vivo</a></li><li><a href=\"/catalog/1511.html\">Смартфоны</a></li><li><a href=\"/catalog/1300.html\">Чехлы для смартфонов Xiaomi</a></li><li><a href=\"/catalog/1302.html\">Защитные стекла для смартфонов Xiaomi</a></li><li><a href=\"/catalog/1310.html\">Чехлы для Huawei/Honor</a></li><li><a href=\"/catalog/1308.html\">Чехлы для смартфонов Samsung</a></li><li><a href=\"/catalog/1307.html\">Защитные стекла для смартфонов Samsung</a></li><li><a href=\"/catalog/1141.html\">Планшеты</a></li><li><a href=\"/catalog/1315.html\">Зарядные устройства и кабели</a></li><li><a href=\"/catalog/1329.html\">Держатели для смартфонов</a></li><li><a href=\"/catalog/665.html\">Автодержатели</a></li><li><a href=\"/catalog/1304.html\">Носимая электроника</a></li><li><a href=\"/catalog/1305.html\">Наушники и колонки</a></li><li><a href=\"/catalog/805.html\">Запчасти для телефонов</a></li><li><a href=\"/catalog/1311.html\">Чехлы для планшетов</a></li><li><a href=\"/catalog/1317.html\">Аксессуары для фото-видео</a></li><li><a href=\"/catalog/1318.html\">Чехлы для смартфонов Apple</a></li><li><a href=\"/catalog/1412.html\">Товары для автомобилистов</a></li><li><a href=\"/catalog/1429.html\">USB Флеш-накопители</a></li><li><a href=\"/catalog/1473.html\">Товары для детей</a></li><li><a href=\"/catalog/1507.html\">Защитные стекла для смартфонов Realme</a></li><li><a href=\"/catalog/1508.html\">Чехлы для смартфонов Realme</a></li><li><a href=\"/catalog/18.html\">Карты памяти</a></li><li><a href=\"/catalog/1303.html\">Защитные стекла для планшетов</a></li><li><a href=\"/catalog/1312.html\">Защитные стекла для смартфонов</a></li>      </ul>\n" +
//                "    </li>\n" +
//                "    <li><a href=\"/dostavka.html\">Доставка</a></li>\n" +
//                "    <li><a href=\"/pickup.html\">Самовывоз</a></li>\n" +
//                "    <li><a href=\"/payment.html\">Оплата</a></li>\n" +
//                "    <li><a href=\"/warranty.html\">Гарантия и обмен</a></li>\n" +
//                "    <li><a href=\"/contacts.html\">Контакты</a></li>\n" +
//                "  </ul>\n" +
//                "</nav>\n" +
//                "\t\n" +
//                "\t\n" +
//                "\t\n" +
//                "\t\n" +
//                "\t\n" +
//                "\t</td>\n" +
//                "  </tr>\n" +
//                "    <td colspan=\"3\" valign=\"top\">\n" +
//                "\t<table width=\"100%\" border=\"0\"  cellpadding=\"0\" cellspacing=\"0\">\n" +
//                "\t<tr><!----<td class=\"menu_full_cell\" width=\"253\">---->\t\t\n" +
//                "\t\t</td>\n" +
//                "    <td colspan=\"2\" class=\"item_full_cell\">\n" +
//                "    <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding-top:15px;\">\n" +
//                " \n" +
//                "      <tr>\n" +
//                "        <td colspan=\"2\">\n" +
//                "          <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">\n" +
//                "\t\t  <tr>\n" +
//                "              <td>\t\t\n" +
//                "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td class=\"catalog_content_cell\" width=\"33%\">\n" +
//                "\t\t\t<table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\">\n" +
//                "            <tr>\n" +
//                "              <td colspan=\"2\" class=\"item_img_cell\">\n" +
//                "\t\t\t<img onClick=\"document.location='/product/1123519.html';return false\" src=\"/img/product/200/1123519_1_200.jpg\"  alt=\"Изображение товара Смартфон realme 10 8/256 ГБ RU, белый\" title=\"Описание и характеристики Смартфон realme 10 8/256 ГБ RU, белый\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\">\n" +
//                "\t\t\t<a href=\"/product/1123519.html\"  title=\"Описание и характеристики Смартфон realme 10 8/256 ГБ RU, белый\" >Смартфон realme 10 8/256 ГБ RU, белый</a>\n" +
//                "\t\t\t</td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              \n" +
//                "\t\t\t \t\t\t  \n" +
//                "\t\t    <td class=\"price_cell\">18180р.</td>\n" +
//                "              <td class=\"item_full_info\" id=\"text1123519\"  onclick=\"addtobasket_w_fancy(1123519)\"><span title=\"Купить Смартфон realme 10 8/256 ГБ RU, белый\"  id=\"buyimg1123519\" class=\"buybutton\">Купить</span></td>\n" +
//                "            </tr>\n" +
//                "            </table></td><td class=\"catalog_content_cell\" width=\"33%\">\n" +
//                "\t\t\t<table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\">\n" +
//                "            <tr>\n" +
//                "              <td colspan=\"2\" class=\"item_img_cell\">\n" +
//                "\t\t\t<img onClick=\"document.location='/product/1123644.html';return false\" src=\"/img/product/200/1123644_1_200.jpg\"  alt=\"Изображение товара Смартфон realme 8 6/128 ГБ Global, Punk Black\" title=\"Описание и характеристики Смартфон realme 8 6/128 ГБ Global, Punk Black\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\">\n" +
//                "\t\t\t<a href=\"/product/1123644.html\"  title=\"Описание и характеристики Смартфон realme 8 6/128 ГБ Global, Punk Black\" >Смартфон realme 8 6/128 ГБ Global, Punk Black</a>\n" +
//                "\t\t\t</td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              \n" +
//                "\t\t\t \t\t\t  \n" +
//                "\t\t    <td class=\"price_cell\">14850р.</td>\n" +
//                "              <td class=\"item_full_info\" id=\"text1123644\"  onclick=\"addtobasket_w_fancy(1123644)\"><span title=\"Купить Смартфон realme 8 6/128 ГБ Global, Punk Black\"  id=\"buyimg1123644\" class=\"buybutton\">Купить</span></td>\n" +
//                "            </tr>\n" +
//                "            </table></td><td class=\"catalog_content_cell\" width=\"33%\">\n" +
//                "\t\t\t<table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\">\n" +
//                "            <tr>\n" +
//                "              <td colspan=\"2\" class=\"item_img_cell\">\n" +
//                "\t\t\t<img onClick=\"document.location='/product/1124076.html';return false\" src=\"/img/product/200/1124076_1_200.jpg\"  alt=\"Изображение товара Смартфон Xiaomi POCO F5 Pro 12/256 ГБ Global, черный\" title=\"Описание и характеристики Смартфон Xiaomi POCO F5 Pro 12/256 ГБ Global, черный\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\">\n" +
//                "\t\t\t<a href=\"/product/1124076.html\"  title=\"Описание и характеристики Смартфон Xiaomi POCO F5 Pro 12/256 ГБ Global, черный\" >Смартфон Xiaomi POCO F5 Pro 12/256 ГБ Global, черный</a>\n" +
//                "\t\t\t</td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              \n" +
//                "\t\t\t \t\t\t  \n" +
//                "\t\t    <td class=\"price_cell\">43950р.</td>\n" +
//                "              <td class=\"item_full_info\" id=\"text1124076\"  onclick=\"addtobasket_w_fancy(1124076)\"><span title=\"Купить Смартфон Xiaomi POCO F5 Pro 12/256 ГБ Global, черный\"  id=\"buyimg1124076\" class=\"buybutton\">Купить</span></td>\n" +
//                "            </tr>\n" +
//                "            </table></td></tr><tr><td class=\"catalog_content_cell\" width=\"33%\">\n" +
//                "\t\t\t<table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\">\n" +
//                "            <tr>\n" +
//                "              <td colspan=\"2\" class=\"item_img_cell\">\n" +
//                "\t\t\t<img onClick=\"document.location='/product/1123211.html';return false\" src=\"/img/product/200/1123211_1_200.jpg\"  alt=\"Изображение товара Смартфон Xiaomi POCO M5s 4/64 ГБ RU, серый\" title=\"Описание и характеристики Смартфон Xiaomi POCO M5s 4/64 ГБ RU, серый\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\">\n" +
//                "\t\t\t<a href=\"/product/1123211.html\"  title=\"Описание и характеристики Смартфон Xiaomi POCO M5s 4/64 ГБ RU, серый\" >Смартфон Xiaomi POCO M5s 4/64 ГБ RU, серый</a>\n" +
//                "\t\t\t</td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              \n" +
//                "\t\t\t \t\t\t  \n" +
//                "\t\t    <td class=\"price_cell\">13450р.</td>\n" +
//                "              <td class=\"item_full_info\" id=\"text1123211\"  onclick=\"addtobasket_w_fancy(1123211)\"><span title=\"Купить Смартфон Xiaomi POCO M5s 4/64 ГБ RU, серый\"  id=\"buyimg1123211\" class=\"buybutton\">Купить</span></td>\n" +
//                "            </tr>\n" +
//                "            </table></td><td class=\"catalog_content_cell\" width=\"33%\">\n" +
//                "\t\t\t<table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\">\n" +
//                "            <tr>\n" +
//                "              <td colspan=\"2\" class=\"item_img_cell\">\n" +
//                "\t\t\t<img onClick=\"document.location='/product/1124079.html';return false\" src=\"/img/product/200/1124079_1_200.jpg\"  alt=\"Изображение товара Смартфон Xiaomi POCO M5s 8/256 ГБ Global, серый\" title=\"Описание и характеристики Смартфон Xiaomi POCO M5s 8/256 ГБ Global, серый\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\">\n" +
//                "\t\t\t<a href=\"/product/1124079.html\"  title=\"Описание и характеристики Смартфон Xiaomi POCO M5s 8/256 ГБ Global, серый\" >Смартфон Xiaomi POCO M5s 8/256 ГБ Global, серый</a>\n" +
//                "\t\t\t</td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              \n" +
//                "\t\t\t \t\t\t  \n" +
//                "\t\t    <td class=\"price_cell\">18950р.</td>\n" +
//                "              <td class=\"item_full_info\" id=\"text1124079\"  onclick=\"addtobasket_w_fancy(1124079)\"><span title=\"Купить Смартфон Xiaomi POCO M5s 8/256 ГБ Global, серый\"  id=\"buyimg1124079\" class=\"buybutton\">Купить</span></td>\n" +
//                "            </tr>\n" +
//                "            </table></td><td class=\"catalog_content_cell\" width=\"33%\">\n" +
//                "\t\t\t<table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\">\n" +
//                "            <tr>\n" +
//                "              <td colspan=\"2\" class=\"item_img_cell\">\n" +
//                "\t\t\t<img onClick=\"document.location='/product/1124078.html';return false\" src=\"/img/product/200/1124078_1_200.jpg\"  alt=\"Изображение товара Смартфон Xiaomi POCO M5s 8/256 ГБ Global, синий\" title=\"Описание и характеристики Смартфон Xiaomi POCO M5s 8/256 ГБ Global, синий\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\">\n" +
//                "\t\t\t<a href=\"/product/1124078.html\"  title=\"Описание и характеристики Смартфон Xiaomi POCO M5s 8/256 ГБ Global, синий\" >Смартфон Xiaomi POCO M5s 8/256 ГБ Global, синий</a>\n" +
//                "\t\t\t</td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              \n" +
//                "\t\t\t \t\t\t  \n" +
//                "\t\t    <td class=\"price_cell\">18950р.</td>\n" +
//                "              <td class=\"item_full_info\" id=\"text1124078\"  onclick=\"addtobasket_w_fancy(1124078)\"><span title=\"Купить Смартфон Xiaomi POCO M5s 8/256 ГБ Global, синий\"  id=\"buyimg1124078\" class=\"buybutton\">Купить</span></td>\n" +
//                "            </tr>\n" +
//                "            </table></td></tr><tr><td class=\"catalog_content_cell\" width=\"33%\">\n" +
//                "\t\t\t<table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\">\n" +
//                "            <tr>\n" +
//                "              <td colspan=\"2\" class=\"item_img_cell\">\n" +
//                "\t\t\t<img onClick=\"document.location='/product/1123400.html';return false\" src=\"/img/product/200/1123400_1_200.jpg\"  alt=\"Изображение товара Смартфон Xiaomi Redmi Note 10 Pro NFC 8/256 ГБ Global, голубой лед\" title=\"Описание и характеристики Смартфон Xiaomi Redmi Note 10 Pro NFC 8/256 ГБ Global, голубой лед\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\">\n" +
//                "\t\t\t<a href=\"/product/1123400.html\"  title=\"Описание и характеристики Смартфон Xiaomi Redmi Note 10 Pro NFC 8/256 ГБ Global, голубой лед\" >Смартфон Xiaomi Redmi Note 10 Pro NFC 8/256 ГБ Global, голубой лед</a>\n" +
//                "\t\t\t</td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              \n" +
//                "\t\t\t \t\t\t  \n" +
//                "\t\t    <td class=\"price_cell\">23250р.</td>\n" +
//                "              <td class=\"item_full_info\" id=\"text1123400\"  onclick=\"addtobasket_w_fancy(1123400)\"><span title=\"Купить Смартфон Xiaomi Redmi Note 10 Pro NFC 8/256 ГБ Global, голубой лед\"  id=\"buyimg1123400\" class=\"buybutton\">Купить</span></td>\n" +
//                "            </tr>\n" +
//                "            </table></td><td class=\"catalog_content_cell\" width=\"33%\">\n" +
//                "\t\t\t<table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\">\n" +
//                "            <tr>\n" +
//                "              <td colspan=\"2\" class=\"item_img_cell\">\n" +
//                "\t\t\t<img onClick=\"document.location='/product/1123368.html';return false\" src=\"/img/product/200/1123368_1_200.jpg\"  alt=\"Изображение товара Смартфон Xiaomi Redmi Note 10 Pro NFC 8/256 ГБ Global, серый оникс\" title=\"Описание и характеристики Смартфон Xiaomi Redmi Note 10 Pro NFC 8/256 ГБ Global, серый оникс\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\">\n" +
//                "\t\t\t<a href=\"/product/1123368.html\"  title=\"Описание и характеристики Смартфон Xiaomi Redmi Note 10 Pro NFC 8/256 ГБ Global, серый оникс\" >Смартфон Xiaomi Redmi Note 10 Pro NFC 8/256 ГБ Global, серый оникс</a>\n" +
//                "\t\t\t</td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              \n" +
//                "\t\t\t \t\t\t  \n" +
//                "\t\t    <td class=\"price_cell\">23250р.</td>\n" +
//                "              <td class=\"item_full_info\" id=\"text1123368\"  onclick=\"addtobasket_w_fancy(1123368)\"><span title=\"Купить Смартфон Xiaomi Redmi Note 10 Pro NFC 8/256 ГБ Global, серый оникс\"  id=\"buyimg1123368\" class=\"buybutton\">Купить</span></td>\n" +
//                "            </tr>\n" +
//                "            </table></td><td class=\"catalog_content_cell\" width=\"33%\">\n" +
//                "\t\t\t<table width=\"250\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom: 15px;\">\n" +
//                "            <tr>\n" +
//                "              <td colspan=\"2\" class=\"item_img_cell\">\n" +
//                "\t\t\t<img onClick=\"document.location='/product/1124050.html';return false\" src=\"/img/product/200/1124050_1_200.jpg\"  alt=\"Изображение товара Смартфон Xiaomi Redmi Note 10 Pro NFC 8/256 ГБ RU, бронзовый градиент\" title=\"Описание и характеристики Смартфон Xiaomi Redmi Note 10 Pro NFC 8/256 ГБ RU, бронзовый градиент\" width=\"200\" style=\"border:none; decoration: none; \"></td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              <td height=\"42\" colspan=\"2\" class=\"catalog_item_label_cell\">\n" +
//                "\t\t\t<a href=\"/product/1124050.html\"  title=\"Описание и характеристики Смартфон Xiaomi Redmi Note 10 Pro NFC 8/256 ГБ RU, бронзовый градиент\" >Смартфон Xiaomi Redmi Note 10 Pro NFC 8/256 ГБ RU, бронзовый градиент</a>\n" +
//                "\t\t\t</td>\n" +
//                "            </tr>\n" +
//                "            <tr>\n" +
//                "              \n" +
//                "\t\t\t \t\t\t  \n" +
//                "\t\t    <td class=\"price_cell\">25300р.</td>\n" +
//                "              <td class=\"item_full_info\" id=\"text1124050\"  onclick=\"addtobasket_w_fancy(1124050)\"><span title=\"Купить Смартфон Xiaomi Redmi Note 10 Pro NFC 8/256 ГБ RU, бронзовый градиент\"  id=\"buyimg1124050\" class=\"buybutton\">Купить</span></td>\n" +
//                "            </tr>\n" +
//                "            </table></td></tr></table>\n" +
//                "\t\t</table>          </td>\n" +
//                "      </tr></table></td>\n" +
//                "  </tr>\n" +
//                "  <tr>\n" +
//                "    <td colspan=\"3\" align=\"center\"><div class=\"footer\">\n" +
//                "<div class=\"footer_block\">\n" +
//                "<span class=\"footer_h1\">Информация</span>\n" +
//                "<br>\n" +
//                "<a href=\"/\">Наши спецпредложения</a>\n" +
//                "<br>\n" +
//                "<a href=\"/dostavka.html\">Доставка</a>\n" +
//                "<br>\n" +
//                "<a href=\"/payment.html\">Оплата</a>\n" +
//                "<br>\n" +
//                "<a href=\"/warranty.html\">Гарантия</a>\n" +
//                "<br>\n" +
//                "<a href=\"/contacts.html\">Контакты</a>\n" +
//                "<br>\n" +
//                "<a href=\"/privacy_policy.html\">Положение о конфиденциальности и защите персональных данных</a>\n" +
//                "</div>\n" +
//                "<div class=\"footer_block_cont\">\n" +
//                "<span class=\"footer_tel\">+7(495)143-77-71</span>\n" +
//                "<br><br>\n" +
//                "<a class=\"footer_email\" href=\"http://vk.com/playback_ru\"  target=\"_blank\"><img src=\"/img/VK.png\" title=\"Наша страница Вконтакте\"></a>\n" +
//                "&nbsp;&nbsp;\n" +
//                "<br><br>\n" +
//                "\n" +
//                "</div>\n" +
//                "<div class=\"footer_block_cont\" style=\"width:260px;\">\n" +
//                "<span class=\"footer_h1\">График работы:</span>\n" +
//                "<br>\n" +
//                "пн-пт: c 11-00 до 20-00\n" +
//                "<br>\n" +
//                "сб-вс: с 11-00 до 18-00\n" +
//                "<br><br>\n" +
//                "<span class=\"footer_h1\">Наш адрес:</span>\n" +
//                "<br>\n" +
//                "Москва, Звездный бульвар, 10,\n" +
//                "<br>\n" +
//                "строение 1, 2 этаж, офис 10.\n" +
//                "</div>\n" +
//                "<div class=\"footer_block\">\n" +
//                "\n" +
//                "</div>\n" +
//                "\n" +
//                "<div class=\"footer_block\">\n" +
//                "<script type=\"text/javascript\" src=\"//vk.com/js/api/openapi.js?105\"></script>\n" +
//                "<div id=\"vk_groups\"></div>\n" +
//                "<script type=\"text/javascript\">\n" +
//                "VK.Widgets.Group(\"vk_groups\", {mode: 0, width: \"260\", height: \"210\", color1: 'FFFFFF', color2: '0C5696', color3: '0064BA'}, 48023501);\n" +
//                "</script>\n" +
//                "</div>\n" +
//                "</div>\n" +
//                "<div style=\"width: 1024px; font-family: Roboto-Regular,Helvetica,sans-serif; text-align: right; font-size: 12px; text-align: left; padding-left: 10px; color: #595959; background: url(/img/footer-fon.png) repeat;\">\n" +
//                "2005-2023 &copy;Интернет магазин PlayBack.ru\n" +
//                "</div>\n" +
//                "<!-- Yandex.Metrika counter -->\n" +
//                "<script type=\"text/javascript\" >\n" +
//                "   (function(m,e,t,r,i,k,a){m[i]=m[i]||function(){(m[i].a=m[i].a||[]).push(arguments)};\n" +
//                "   m[i].l=1*new Date();k=e.createElement(t),a=e.getElementsByTagName(t)[0],k.async=1,k.src=r,a.parentNode.insertBefore(k,a)})\n" +
//                "   (window, document, \"script\", \"https://mc.yandex.ru/metrika/tag.js\", \"ym\");\n" +
//                "\n" +
//                "   ym(232370, \"init\", {\n" +
//                "        clickmap:true,\n" +
//                "        trackLinks:true,\n" +
//                "        accurateTrackBounce:true,\n" +
//                "        webvisor:true\n" +
//                "   });\n" +
//                "</script>\n" +
//                "<noscript><div><img src=\"https://mc.yandex.ru/watch/232370\" style=\"position:absolute; left:-9999px;\" alt=\"\" /></div></noscript>\n" +
//                "<!-- /Yandex.Metrika counter -->\n" +
//                "<!-- BEGIN JIVOSITE CODE {literal} -->\n" +
//                "<script type='text/javascript'>\n" +
//                "(function(){ var widget_id = '8LKJc6dMce';var d=document;var w=window;function l(){\n" +
//                "  var s = document.createElement('script'); s.type = 'text/javascript'; s.async = true;\n" +
//                "  s.src = '//code.jivosite.com/script/widget/'+widget_id\n" +
//                "    ; var ss = document.getElementsByTagName('script')[0]; ss.parentNode.insertBefore(s, ss);}\n" +
//                "  if(d.readyState=='complete'){l();}else{if(w.attachEvent){w.attachEvent('onload',l);}\n" +
//                "  else{w.addEventListener('load',l,false);}}})();\n" +
//                "</script>\n" +
//                "<!-- {/literal} END JIVOSITE CODE -->\n" +
//                "</td>\n" +
//                "  </tr>\n" +
//                "</table>\n" +
//                "<a href=\"#\" class=\"scrollup\">Наверх</a>\n" +
//                "</body>\n" +
//                "</html>";
//        HashMap<String,Integer> map = returnLemmas.getLemmas(text);
//        for (Map.Entry<String,Integer> entry : map.entrySet()){
//            System.out.println(entry.getKey() + " - " + entry.getValue());
//        }

    }

}
