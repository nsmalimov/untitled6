import copy
import psycopg2

ip_addresses = [
    "81.89.188.0/23", "217.197.9.0/24", "172.26.0.0/22",
    "81.89.186.0/23", "172.26.4.0/22",
    "81.89.178.0/23", "172.26.8.0/22",
    "81.89.176.0/23", "172.26.12.0/22",
    "81.89.180.0/24", "172.26.16.0/22",
    "217.197.12.0/24", "172.26.64.0/22",
    "92.42.30.0/24", "172.26.68.0/22",
    "92.42.28.0/24", "172.26.72.0/22",
    "92.42.31.0/24", "92.42.29.0/25", "172.26.76.0/22",
    "92.42.26.0/23", "217.197.10.0/24", "172.26.80.0/22",
    "217.197.0.0/23", "217.197.7.0/24", "172.26.84.0/22",
    "217.197.6.0/23", "172.26.88.0/22",
    "81.89.181.0/24", "172.26.20.0/22",
    "195.70.216.0/24", "172.26.24.0/22",
    "81.89.185.0/24", "172.26.28.0/22",
    "217.197.11.0/24", #"92.42.29.128/25",
    "172.26.92.0/22",
    "217.197.2.0/23", "172.26.96.0/22",
    "217.197.4.0/23", "172.26.100.0/22",
    "217.197.8.0/24", "172.26.104.0/22"]

def prepare_ip(ip_address):
    new_ip = []

    split_ip = ip_address.split("/")

    ip = split_ip[0]
    #print ip

    if (split_ip[1] == "24" or split_ip[1] == "25"):
       return [ip[0:-2]]

    if (split_ip[1] == "23"):
        new_ip.append(ip[0:-2])

        split_sub_ip = ip[0:-2].split(".")

        split_sub_ip[-1] = str(int(split_sub_ip[-1]) + 1)

        new_ip.append('.'.join(i for i in split_sub_ip))

    if (split_ip[1] == "22"):
        new_ip.append(ip[0:-2])

        split_sub_ip = ip[0:-2].split(".")

        split_sub_ip[-1] = str(int(split_sub_ip[-1]) + 1)

        new_ip.append('.'.join(i for i in split_sub_ip))   
        
        split_sub_ip[-1] = str(int(split_sub_ip[-1]) + 1)

        new_ip.append('.'.join(i for i in split_sub_ip))  

        split_sub_ip[-1] = str(int(split_sub_ip[-1]) + 1)

        new_ip.append('.'.join(i for i in split_sub_ip))   

    return new_ip

def generate_ip():
    prepeared_array = []

    for i in ip_addresses:
        new_ip = prepare_ip(copy.deepcopy(i))
    
        for j in new_ip:
            prepeared_array.append(copy.deepcopy(j))

    prepeared_array.append("92.42.29")

    return prepeared_array


def insert_to_db(generated_array):

    conn = psycopg2.connect("dbname='videochat' user='postgres' host='localhost' password='ve;br'")
    
    cur = conn.cursor()    

    for i in generated_array:
        
        query =  "INSERT INTO usersIP (ip) VALUES (%s);"
        data = (i,)
        #print data

        cur.execute(query, data)

    conn.commit()
    conn.close()  

generated_array = generate_ip()

#for i in generated_array:
#   print i

generated_array = list(set(generated_array))
insert_to_db(generated_array)      