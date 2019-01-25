from flask import Flask
from flask_restful import Resource, Api, reqparse
import requests
import json
from urllib.request import urlopen
from bs4 import BeautifulSoup
from lxml import html


app = Flask(__name__)
api = Api(app)


# 현재 시세 정보
class MarketData(Resource):
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument('code',type=str)
        args = parser.parse_args()

        code_data = args['code']
        #print(code_data)

        url = 'https://sandbox-apigw.koscom.co.kr/v2/market/stocks/{marketcode}/{issuecode}/price'.replace('{marketcode}', 'kospi').replace('{issuecode}',str(code_data))+'?'+'apikey'+'=l7xx30a19d389f204686a4b2a0e150ade045'

        response = requests.get(url)

        result= json.loads(response.content)
        try:
            price =  result['result']['trdPrc']
        except KeyError:
            price = 0

        return {'price_now': price}

api.add_resource(MarketData,'/price')


# 기업 세부정보 크롤링
class Company(Resource):
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument('code',type=str)
        args = parser.parse_args()
        code_data = args['code']
        url = "https://finance.naver.com/item/main.nhn?code="+str(code_data)
        html = urlopen(url)
        bsObject = BeautifulSoup(html, "html.parser")
        html.close()
        data = {}
        market_sum = bsObject.find('em',{'id':'_market_sum'})

        data["market_sum"] = str(market_sum)[60:-34]
        per = bsObject.find('em',{'id':'_per'})
        data["per"]=str(per)[14:-5]
        dvr = bsObject.find('em',{'id':'_pbr'})
        data["dvr"]=str(dvr)[14:-5]
        return data

api.add_resource(Company,'/table')

# 관련 기업 정보 크롤링
class RelatedCompany(Resource):
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument('code',type=str)
        args = parser.parse_args()

        code_data = args['code']
        url = "https://finance.naver.com/item/main.nhn?code="+str(code_data)
        html = urlopen(url)
        bsObject = BeautifulSoup(html, "html.parser")
        html.close()
        related_table = bsObject.find(summary="동종업종 비교에 관한표이며 종목명에 따라 정보를 제공합니다.")
        table = related_table.find("thead")
        trs = table.find_all('a')
        value =[]
        for tr in trs:
            tr = str(tr)
            tr = tr[37:-46]
            value.append(tr)
        #print(table)
        data ={"related_company": value}
        return data

api.add_resource(RelatedCompany,'/related')

if __name__ =='__main__':
    app.run(debug=True)


"""



"""