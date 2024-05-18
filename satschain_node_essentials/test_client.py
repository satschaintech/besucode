import rlp
import requests

def to_integer(s) :
    if isinstance(s, str) :
        if(s.startswith("0x")) :
            s = s[2:]
        s = int(s, 16)
    if not isinstance(s, int) :
        s = 0
    return s

def to_integer_bytes(s, bytes_count) :
    if isinstance(s, str) :
        if(s.startswith("0x")) :
            s = s[2:]
        s = int(s, 16)
    if not isinstance(s, int) :
        s = 0
    return s.to_bytes(bytes_count, byteorder='big')

def to_binary(b) :
    if isinstance(b, str) :
        if(b.startswith("0x")) :
            b = b[2:]
        b = bytes.fromhex(b)
    if not isinstance(b, bytes) :
        b = b""
    return b

def get_raw_transaction(chain_id, nonce, gas_limit, to_addr, value, data, record_no, from_addr) :
    nonce = to_integer(nonce)
    gas_price = to_integer(0)
    gas_limit = to_integer(gas_limit)
    if(to_addr == None) :
        to_addr = b''
    else:
        to_addr = to_integer_bytes(to_addr, 20)
    value = to_integer(value)
    data = to_binary(data)
    record_no = to_integer(record_no)
    from_addr = to_integer(from_addr)
    v = chain_id * 2 + 35 # giving besu compatible rec_id = 0
    r = record_no
    s = from_addr
    # below is the same order the transaction should be encoded in
    bin_tx = rlp.encode([nonce, gas_price, gas_limit, to_addr, value, data, v, r, s])
    print(bin_tx)
    return "0x" + bin_tx.hex()

request_id = 1

def send_web3_request(method, params) :
    global request_id
    resp = requests.post("http://localhost:8545", json = {
        "jsonrpc" : "2.0",
        "method" : method,
        "params" : params,
        "id" : request_id
    })
    request_id += 1
    return resp.text

r1 = send_web3_request("txpool_clear", [])
print(r1)

tx = get_raw_transaction(chain_id=51415, nonce=1, gas_limit=0x21000, to_addr=0x1a45e15830052d4c441f466c4d1d92a878aa2aa5, value=0x200000, data=b"", record_no=3, from_addr=0xbc936ceba516e86ace90c087eb702ff064e6c73d)
print(tx)
r1 = send_web3_request("eth_sendRawTransaction", [tx])
print(r1)

tx = get_raw_transaction(chain_id=51415, nonce=0, gas_limit=0x21000, to_addr=0xbc936ceba516e86ace90c087eb702ff064e6c73d, value=0x300000, data=b"", record_no=2, from_addr=0x1a45e15830052d4c441f466c4d1d92a878aa2aa5)
print(tx)
r1 = send_web3_request("eth_sendRawTransaction", [tx])
print(r1)

tx = get_raw_transaction(chain_id=51415, nonce=1, gas_limit=0x21000, to_addr=0x03, value=0x100000, data=b"", record_no=4, from_addr=0x1a45e15830052d4c441f466c4d1d92a878aa2aa5)
print(tx)
r1 = send_web3_request("eth_sendRawTransaction", [tx])
print(r1)

tx = get_raw_transaction(chain_id=51415, nonce=0, gas_limit=0x21000, to_addr=0x1a45e15830052d4c441f466c4d1d92a878aa2aa5, value=0x200000, data=b"", record_no=1, from_addr=0xbc936ceba516e86ace90c087eb702ff064e6c73d)
print(tx)
r1 = send_web3_request("eth_sendRawTransaction", [tx])
print(r1)

r1 = send_web3_request("txpool_clear", [])
print(r1)

r1 = send_web3_request("miner_mineBulkSynchronously", [0x123, 1])
print(r1)