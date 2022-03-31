pragma solidity ^0.6.0;

interface ParallelConfigPrecompiled
{
    function registerParallelFunctionInternal(address, string memory, uint256) public virtual returns (int);
    function unregisterParallelFunctionInternal(address, string memory) public virtual returns (int);
}

interface ParallelContract
{
    ParallelConfigPrecompiled precompiled = ParallelConfigPrecompiled(0x1006);
    
    function registerParallelFunction(string memory functionName, uint256 criticalSize) public
    {
        precompiled.registerParallelFunctionInternal(address(this), functionName, criticalSize);
    }
    
    function unregisterParallelFunction(string memory functionName) public
    {
        precompiled.unregisterParallelFunctionInternal(address(this), functionName);
    }
    
    function enableParallel() public virtual;
    function disableParallel() public virtual;
}