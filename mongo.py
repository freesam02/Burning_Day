from pymongo import MongoClient
import requests
import json
from time import sleep


def update_recommend(company_name):
    #들어갈 기업명들 강제로 채워야 함
    f = open("json_note.txt", 'r',encoding='utf-8-sig')
    company_data = f.read()
    company_info = json.loads(company_data)
    f.close()
    url ="http://127.0.0.1:8080/recommend"+"?id="+str(company_info[company_name])

    response = requests.get(url)
    print(response.content)

    data = response.content.decode('utf-8')
    client = MongoClient("127.0.0.1", 27017)
    db = client.shop
    collection = db.products
    myquery = {"meta_description": str(company_info[company_name])}
    new_values = {"$set": {"description" : data}}
    collection.update_one(myquery, new_values)

def update_price(company_name):
    #들어갈 기업명들 강제로 채워야 함
    f = open("json_note.txt", 'r',encoding='utf-8-sig')
    company_data = f.read()
    company_info = json.loads(company_data)
    f.close()
    url ="http://127.0.0.1:5000/price"+"?code="+str(company_info[company_name])

    response = requests.get(url)
    print(response.content)
    result = json.loads(response.content)
    price = result['price_now']
    client = MongoClient("127.0.0.1", 27017)
    db = client.shop
    collection = db.products
    myquery = {"meta_description": str(company_info[company_name])}
    new_values = {"$set": {"regular_price" : price}}
    collection.update_one(myquery, new_values)

def update_info(company_name):
    f = open("json_note.txt", 'r',encoding='utf-8-sig')
    company_data = f.read()
    company_info = json.loads(company_data)
    f.close()
    #들어갈 기업명들 강제로 채워야 함

    # 정보 포함 해야함
    url ="http://127.0.0.1:5000/table"+"?code="+str(company_info[company_name])
    response = requests.get(url)
    result = json.loads(response.content)

    url2 = "http://127.0.0.1:5000/related"+"?code="+str(company_info[company_name])
    response = requests.get(url2)
    result2 = json.loads(response.content)
    data = []
    for key, item in result.items():
        temp={}
        temp["name"]=key
        temp["value"]=item
        data.append(temp)

    for key, item in result2.items():
        print(item)
        temp={}
        temp["name"]=key
        temp["value"]=str(item)[1:-1]
        data.append(temp)


    client = MongoClient("127.0.0.1", 27017)
    db = client.shop
    collection = db.products
    myquery = {"meta_description": str(company_info[company_name])}
    new_values = {"$set": {"attributes" : data}}
    print(collection.update_one(myquery, new_values))


f= open("json_note.txt",'r',encoding='utf-8-sig')

company_data = f.read()
company_info = json.loads(company_data)
print(company_info)
f.close()
print("Server Start")
while(True):
    print("Update Database")
    for product in company_info.keys():
        update_price(product)
        update_info(product)
        update_recommend(product)

    sleep(1)
