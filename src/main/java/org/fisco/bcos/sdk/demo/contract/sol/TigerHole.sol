pragma solidity ^0.6.3;

contract TigerHole {
    struct Tigers {
        int[] tigers;
    }
    
    mapping (string => Tigers) openid2Tigers;
    mapping (int => string) tiger2OpenID;
    
    // 设置openid和老虎的关联关系，发放、衍生、合成老虎时使用
    function setTiger(string memory openid, int tiger) public returns(bool){
        if(bytes(tiger2OpenID[tiger]).length > 0) {
            return false;
        }
        
        openid2Tigers[openid].tigers.push(tiger);
        tiger2OpenID[tiger] = openid;
        
        return true;
    }
    
    // 通过openid获取老虎列表
    function getTigersByOpenID(string memory openid) public view returns(int[] memory) {
        return openid2Tigers[openid].tigers;
    }
    
    // 通过老虎id获取openid
    function getOpenIDByTiger(int tiger) public view returns(string memory) {
        return tiger2OpenID[tiger];
    }
}