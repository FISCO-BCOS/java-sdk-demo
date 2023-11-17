import "./CodeOp.sol";
import "./Create2.sol";
import "./DelegateCall.sol";
import "./EventTest.sol";
import "./LibraryTest.sol";
import "./ProxyTest.sol";
import "./StaticCall.sol";

contract ContractTestAll {

    function checkOne(string memory name, address addr) private {
        try ContractTestAll(addr).check() { // just a little trick to call check() in the target contract
            // success
        } catch (bytes memory reason) {
            require(false, name);
        }
    }

    function check() public {
        checkOne("CodeOp", address(new CodeOp()));
        checkOne("Create2", address(new Create2()));
        checkOne("DelegateCall", address(new DelegateCall()));
        checkOne("EventTest", address(new EventTest()));
        checkOne("LibraryTest", address(new LibraryTest()));
        checkOne("ProxyTest", address(new ProxyTest()));
        checkOne("StaticCall", address(new StaticCall()));
    }
}
