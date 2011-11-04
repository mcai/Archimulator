CacheMiss handleReplacement(ref){
        if(ref.read&&ref.fromDelinquentPc&&ref.fromMT){
        return new CacheMiss(ref,-1);
}

        return new CacheMiss(ref,getLRU(ref.set));
}

        void handlPromotionOnHit(hit){
        if(hit.ref.read&&hit.line.ht&&hit.ref.fromMT){
        setLRU(hit.ref.set,hit.way);
hit.line.ht=false;
}
        else{
        setMRU(hit.ref.set,hit.way);
}
        }

        void handleInsertionOnMiss(miss){
        miss.line.ht=false;

if(miss.ref.read&&miss.ref.fromDelinquentPc){
        if(miss.ref.fromMT){
        setLRU(miss.ref.set,miss.way);
}
        else if(miss.ref.fromHT){
        setMRU(miss.ref.set,miss.way);
miss.ref.ht=true;
}
        else{
        setMRU(miss.ref.set,miss.way);
}
        }
        else{
        setMRU(miss.ref.set,miss.way);
}
        }

