"""Lossless native structure NBT access for targeted terrain upgrades."""
import gzip
import io
import struct

def read(path):
    stream=io.BytesIO(gzip.decompress(path.read_bytes()))
    def number(fmt):return struct.unpack('>'+fmt,stream.read(struct.calcsize('>'+fmt)))[0]
    def text():return stream.read(number('H')).decode('utf-8')
    def value(kind):
        if kind in (1,2,3,4,5,6):return number({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[kind])
        if kind==7:return stream.read(number('i'))
        if kind==8:return text()
        if kind==9:
            child=number('B');return child,[value(child) for _ in range(number('i'))]
        if kind==10:
            result={}
            while (child:=number('B')):
                name=text();result[name]=(child,value(child))
            return result
        if kind in (11,12):return [number('i' if kind==11 else 'q') for _ in range(number('i'))]
        raise ValueError(kind)
    assert number('B')==10
    text();return value(10)

def write(path,root):
    def text(value):
        data=value.encode('utf-8');return struct.pack('>H',len(data))+data
    def value(kind,v):
        if kind in (1,2,3,4,5,6):return struct.pack('>'+{1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[kind],v)
        if kind==7:return struct.pack('>i',len(v))+v
        if kind==8:return text(v)
        if kind==9:return bytes([v[0]])+struct.pack('>i',len(v[1]))+b''.join(value(v[0],e) for e in v[1])
        if kind==10:return b''.join(bytes([k])+text(n)+value(k,e) for n,(k,e) in v.items())+b'\0'
        if kind in (11,12):return struct.pack('>i',len(v))+b''.join(struct.pack('>i' if kind==11 else '>q',n) for n in v)
        raise ValueError(kind)
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_bytes(gzip.compress(b'\x0a\0\0'+value(10,root),mtime=0))

def blocks(root):
    palette=root['palette'][1][1]
    result={}
    for entry in root['blocks'][1][1]:
        state=palette[entry['state'][1]]
        result[tuple(entry['pos'][1][1])]=(state['id'][1],{k:v[1] for k,v in state.get('properties',(10,{}))[1].items()},entry.get('nbt',(10,None))[1])
    return result

def replace_blocks(root,blocks):
    palette,entries,indexes=[],[],{}
    for pos,(block,props,nbt) in sorted(blocks.items()):
        key=block,tuple(sorted(props.items()))
        if key not in indexes:
            indexes[key]=len(palette);state={'id':(8,block)}
            if props:state['properties']=(10,{k:(8,v) for k,v in props.items()})
            palette.append(state)
        entry={'pos':(9,(3,list(pos))),'state':(3,indexes[key])}
        if nbt is not None:entry['nbt']=(10,nbt)
        entries.append(entry)
    root['palette']=(9,(10,palette));root['blocks']=(9,(10,entries))
