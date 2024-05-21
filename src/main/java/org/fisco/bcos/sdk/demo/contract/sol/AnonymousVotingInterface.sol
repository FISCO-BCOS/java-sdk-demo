// Copyright (C) @2014-2022 Webank
// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.6.10 <0.8.20;
pragma experimental ABIEncoderV2;

import "./ZkpPrecompiled.sol";
import "./WedprUtils.sol";
import "./Crypto.sol";

    struct SystemParameters {
        int256 itemId; // the itemId
        string itemMeta; // item meta data
        string[] candidates;
        bytes hpoint;
    }

    struct Ballot{
        string candidateId;
        bytes cipher1;
        bytes cipher2;
        bool isAssigned;
    }
// the ballot proof
    struct BallotProof{
        bytes formatProof;
        bytes eitherEqualityProof;
        Ballot ballot;
    }

    struct UnlistedBallot
    {
        bytes cipher1;
        bytes cipher2;
        Ballot ballot;
        bool isAssigned;
    }
// the candiate ballot proof
    struct UnlistedCandidateBallotProof
    {
        UnlistedBallot ballot;
        BallotProof unlisted_ballot_proof;
    }
    struct VoteRequest{
        Ballot blankBallot;
        Ballot zeroBallot;
        // bytes publicKey;
        // bytes32 signatureR;
        // bytes32 signatureS;
    }
    struct VoteStorage{
        bool isAssigned;
        VoteRequest voteRequest;
        string[] candidateListForBallot;
        bytes[] cipherCandidateListForUnlistedBallot;
    }
    struct CounterStorage
    {
        // 计票公钥分片
        bytes hpointShare;
        // 计票公钥是否被聚合
        bool setted;
        // 计票服务状态，1：计票中，2：计票完成
        int8 counterStatus;
        // 计票服务实例的心跳时间
        uint256 updateTime;
        // 一个投票item中投票密文的聚合值
        string voteStorageSum;
        // 使用acv-core的公钥加密的计票服务sm4密钥
        string sm4SecretKeyCipherText;
        // 使用计票服务的sm4密钥加密的计票分片
        string countingPartResultCipherText;
        // 计票分片哈希值
        bytes decryptedPartResultHash;
    }

contract AnonymousVotingInterface {
    function initialize(bytes memory basePointG1, bytes memory basePointG2) public virtual;
    function setSystemParameters(int256 itemId, string memory itemMeta, string[] memory candidates, bytes memory hpoint) public virtual;
    function getSystemParameters() public view virtual returns(SystemParameters memory);
    function setCounterNumber(uint256 counterNumber) public virtual;
    function getCounterNumber() public view virtual returns(uint256);
    function setContractState(uint8 state) public virtual;
    function getContractState() public view virtual returns(uint8);
    function setVoterIdList(string[] memory voterIdList) public virtual;
    function getVoterIdList() public view virtual returns(string[] memory);
    function setVoterIdToVoteStorage(string memory voterId, VoteStorage memory voteStorage) public virtual;
    function getVoterIdToVoteStorage(string memory voterId) public view virtual returns(VoteStorage memory);
    function setVoterIDToCandidateIDToBallot(string memory voterId, string memory candidateId, Ballot memory ballot) public virtual;
    function getVoterIDToCandidateIDToBallot(string memory voterId, string memory candidateId) public view virtual returns(Ballot memory);
    function setVoterIDToCandidateIDToUnlistedBallot(string memory voterId, bytes memory candidateId, UnlistedBallot memory unlistedBallot) public virtual;
    function getVoterIDToCandidateIDToUnlistedBallot(string memory voterId, bytes memory candidateId) public view virtual returns(UnlistedBallot memory);
    function setCounterIdToCounterStorage(string memory counterId, CounterStorage memory counterStorage) public virtual;
    function getCounterIdToCounterStorage(string memory counterId) public view virtual returns(CounterStorage memory);
    function setCounterStatus(string memory counterId, int8 status) public virtual;
    function getCounterStatus(string memory counterId) public view virtual returns(int8);
    function setCounterUpdateTime(string memory counterId, uint256 updateTime) public virtual;
    function getCounterUpdateTime(string memory counterId) public view virtual returns(uint256);
    function setVoteStorageSum(string memory counterId, string memory voteStorageSum) public virtual;
    function getVoteStorageSum(string memory counterId) public view virtual returns(string memory);
    function setSm4SecretKeyCipherText(string memory counterId, string memory sm4SecretKeyCipherText) public virtual;
}
