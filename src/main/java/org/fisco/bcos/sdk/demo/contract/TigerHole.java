package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Bool;
import org.fisco.bcos.sdk.abi.datatypes.DynamicArray;
import org.fisco.bcos.sdk.abi.datatypes.Event;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.Utf8String;
import org.fisco.bcos.sdk.abi.datatypes.generated.Int256;
import org.fisco.bcos.sdk.abi.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple4;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple6;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.eventsub.EventCallback;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class TigerHole extends Contract {
    public static final String[] BINARY_ARRAY = {
        "6080604052610400600055609660015560006006557fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff6007557ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe6008557ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffd6009557ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc600a557ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffb600b557ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffa600c557ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff9600d557ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8600e5534801561014057600080fd5b506111da806101506000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c806331feb67114610051578063bca926af14610084578063c70fa50a1461008e578063f350cf1f146100be575b600080fd5b61006b60048036038101906100669190610b0a565b6100f0565b60405161007b9493929190610ef5565b60405180910390f35b61008c6102cd565b005b6100a860048036038101906100a39190610b4b565b610366565b6040516100b59190610f48565b60405180910390f35b6100d860048036038101906100d39190610b0a565b610896565b6040516100e793929190610f63565b60405180910390f35b6060806000806002856040516101069190610e7e565b90815260200160405180910390206000016002866040516101279190610e7e565b90815260200160405180910390206001016002876040516101489190610e7e565b90815260200160405180910390206002015460028860405161016a9190610e7e565b908152602001604051809103902060030160009054906101000a900460ff16838054806020026020016040519081016040528092919081815260200182805480156101d457602002820191906000526020600020905b8154815260200190600101908083116101c0575b5050505050935082805480602002602001604051908101604052809291908181526020016000905b828210156102b8578382906000526020600020018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102a45780601f10610279576101008083540402835291602001916102a4565b820191906000526020600020905b81548152906001019060200180831161028757829003601f168201915b5050505050815260200190600101906101fc565b50505050925093509350935093509193509193565b600061100690508073ffffffffffffffffffffffffffffffffffffffff16630553904e3060046040518363ffffffff1660e01b8152600401610310929190610eb9565b602060405180830381600087803b15801561032a57600080fd5b505af115801561033e573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906103629190610ae1565b5050565b600080875114806103775750600086145b1561038657600754905061088c565b60006004600088815260200190815260200160002060000180546001816001161561010002031660029004905011156103c357600854905061088c565b6000546002886040516103d69190610e7e565b908152602001604051809103902060000180549050106103fa57600954905061088c565b6000821415610480576002876040516104139190610e7e565b908152602001604051809103902060030160009054906101000a900460ff161561044157600c54905061088c565b60016002886040516104539190610e7e565b908152602001604051809103902060030160006101000a81548160ff021916908315150217905550610781565b60018214156107675760015460028660405161049c9190610e7e565b908152602001604051809103902060020154106104bd57600a54905061088c565b60006003856040516104cf9190610e7e565b908152602001604051809103902060010154141561054457846003856040516104f89190610e7e565b9081526020016040518091039020600001908051906020019061051c9291906109a9565b508260038560405161052e9190610e7e565b9081526020016040518091039020600101819055505b6003846040516105549190610e7e565b9081526020016040518091039020600101546003856040516105769190610e7e565b9081526020016040518091039020600201541061059757600b54905061088c565b600084886040516020016105ac929190610e95565b604051602081830303815290604052805190602001209050600115156005600083815260200190815260200160002060009054906101000a900460ff16151514156105fc57600d5491505061088c565b846004600089815260200190815260200160002060010190805190602001906106269291906109a9565b506002866040516106379190610e7e565b908152602001604051809103902060020160008154600101919050819055506003856040516106669190610e7e565b9081526020016040518091039020600201600081546001019190508190555060016005600083815260200190815260200160002060006101000a81548160ff0219169083151502179055507f87609a1c514cae23b77b4fd88af05026dcd719531ae71a861f01fa806ba266a66002876040516106e29190610e7e565b9081526020016040518091039020600201546040516107019190610fa1565b60405180910390a17f708fc77dd8134f699822af898790d3337a8bb796d5ee813482ef02928717f11160038660405161073a9190610e7e565b9081526020016040518091039020600201546040516107599190610fa1565b60405180910390a150610780565b60028214156107755761077f565b600e54905061088c565b5b5b866004600088815260200190815260200160002060000190805190602001906107ab9291906109a9565b506002876040516107bc9190610e7e565b90815260200160405180910390206000018690806001815401808255809150506001900390600052602060002001600090919091909150557ffd341493d5eeef63777b9b32d0bc62268694f851c0904b761347a998e69a96a46002886040516108259190610e7e565b9081526020016040518091039020600001805490506040516108479190610fa1565b60405180910390a17f048e8d4028ef03595be7b543b52980c60dabb6ab2ecf1b665e5f63c705cc28558660405161087e9190610f48565b60405180910390a160065490505b9695505050505050565b60606000806003846040516108ab9190610e7e565b90815260200160405180910390206000016003856040516108cc9190610e7e565b9081526020016040518091039020600101546003866040516108ee9190610e7e565b908152602001604051809103902060020154828054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156109955780601f1061096a57610100808354040283529160200191610995565b820191906000526020600020905b81548152906001019060200180831161097857829003601f168201915b505050505092509250925092509193909250565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106109ea57805160ff1916838001178555610a18565b82800160010185558215610a18579182015b82811115610a175782518255916020019190600101906109fc565b5b509050610a259190610a29565b5090565b610a4b91905b80821115610a47576000816000905550600101610a2f565b5090565b90565b600081359050610a5d81611176565b92915050565b600081519050610a7281611176565b92915050565b600082601f830112610a8957600080fd5b8135610a9c610a9782610fe9565b610fbc565b91508082526020830160208301858383011115610ab857600080fd5b610ac3838284611123565b50505092915050565b600081359050610adb8161118d565b92915050565b600060208284031215610af357600080fd5b6000610b0184828501610a63565b91505092915050565b600060208284031215610b1c57600080fd5b600082013567ffffffffffffffff811115610b3657600080fd5b610b4284828501610a78565b91505092915050565b60008060008060008060c08789031215610b6457600080fd5b600087013567ffffffffffffffff811115610b7e57600080fd5b610b8a89828a01610a78565b9650506020610b9b89828a01610a4e565b955050604087013567ffffffffffffffff811115610bb857600080fd5b610bc489828a01610a78565b945050606087013567ffffffffffffffff811115610be157600080fd5b610bed89828a01610a78565b9350506080610bfe89828a01610acc565b92505060a0610c0f89828a01610a4e565b9150509295509295509295565b6000610c288383610d39565b60208301905092915050565b6000610c408383610d66565b905092915050565b610c51816110bf565b82525050565b6000610c6282611035565b610c6c8185611070565b9350610c7783611015565b8060005b83811015610ca8578151610c8f8882610c1c565b9750610c9a83611056565b925050600181019050610c7b565b5085935050505092915050565b6000610cc082611040565b610cca8185611081565b935083602082028501610cdc85611025565b8060005b85811015610d185784840389528151610cf98582610c34565b9450610d0483611063565b925060208a01995050600181019050610ce0565b50829750879550505050505092915050565b610d33816110d1565b82525050565b610d42816110dd565b82525050565b610d51816110dd565b82525050565b610d6081611111565b82525050565b6000610d718261104b565b610d7b8185611092565b9350610d8b818560208601611132565b610d9481611165565b840191505092915050565b6000610daa8261104b565b610db481856110a3565b9350610dc4818560208601611132565b610dcd81611165565b840191505092915050565b6000610de38261104b565b610ded81856110b4565b9350610dfd818560208601611132565b80840191505092915050565b6000610e166036836110a3565b91507f7472616465546967657228737472696e672c696e743235362c737472696e672c60008301527f737472696e672c75696e743235362c696e7432353629000000000000000000006020830152604082019050919050565b610e7881611107565b82525050565b6000610e8a8284610dd8565b915081905092915050565b6000610ea18285610dd8565b9150610ead8284610dd8565b9150",
        "8190509392505050565b6000606082019050610ece6000830185610c48565b8181036020830152610edf81610e09565b9050610eee6040830184610d57565b9392505050565b60006080820190508181036000830152610f0f8187610c57565b90508181036020830152610f238186610cb5565b9050610f326040830185610e6f565b610f3f6060830184610d2a565b95945050505050565b6000602082019050610f5d6000830184610d48565b92915050565b60006060820190508181036000830152610f7d8186610d9f565b9050610f8c6020830185610e6f565b610f996040830184610e6f565b949350505050565b6000602082019050610fb66000830184610e6f565b92915050565b6000604051905081810181811067ffffffffffffffff82111715610fdf57600080fd5b8060405250919050565b600067ffffffffffffffff82111561100057600080fd5b601f19601f8301169050602081019050919050565b6000819050602082019050919050565b6000819050602082019050919050565b600081519050919050565b600081519050919050565b600081519050919050565b6000602082019050919050565b6000602082019050919050565b600082825260208201905092915050565b600082825260208201905092915050565b600082825260208201905092915050565b600082825260208201905092915050565b600081905092915050565b60006110ca826110e7565b9050919050565b60008115159050919050565b6000819050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b600061111c82611107565b9050919050565b82818337600083830152505050565b60005b83811015611150578082015181840152602081019050611135565b8381111561115f576000848401525b50505050565b6000601f19601f8301169050919050565b61117f816110dd565b811461118a57600080fd5b50565b61119681611107565b81146111a157600080fd5b5056fea2646970667358221220c1592489dfb0ca622f4485a45ef4fb6f20646c3534cd5b2fb7a930c2e1c29fa464736f6c634300060a0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "6080604052610400600055609660015560006006557fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff6007557ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe6008557ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffd6009557ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc600a557ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffb600b557ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffa600c557ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff9600d557ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8600e5534801561014057600080fd5b506111da806101506000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c806394618e4c14610051578063b879a4c71461005b578063d63216d21461008e578063dff30bc0146100be575b600080fd5b6100596100f0565b005b61007560048036038101906100709190610b0a565b610189565b6040516100859493929190610ef5565b60405180910390f35b6100a860048036038101906100a39190610b4b565b610366565b6040516100b59190610f48565b60405180910390f35b6100d860048036038101906100d39190610b0a565b610896565b6040516100e793929190610f63565b60405180910390f35b600061100690508073ffffffffffffffffffffffffffffffffffffffff1663dc536a623060046040518363ffffffff1660e01b8152600401610133929190610eb9565b602060405180830381600087803b15801561014d57600080fd5b505af1158015610161573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906101859190610ae1565b5050565b60608060008060028560405161019f9190610e7e565b90815260200160405180910390206000016002866040516101c09190610e7e565b90815260200160405180910390206001016002876040516101e19190610e7e565b9081526020016040518091039020600201546002886040516102039190610e7e565b908152602001604051809103902060030160009054906101000a900460ff168380548060200260200160405190810160405280929190818152602001828054801561026d57602002820191906000526020600020905b815481526020019060010190808311610259575b5050505050935082805480602002602001604051908101604052809291908181526020016000905b82821015610351578382906000526020600020018054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561033d5780601f106103125761010080835404028352916020019161033d565b820191906000526020600020905b81548152906001019060200180831161032057829003601f168201915b505050505081526020019060010190610295565b50505050925093509350935093509193509193565b600080875114806103775750600086145b1561038657600754905061088c565b60006004600088815260200190815260200160002060000180546001816001161561010002031660029004905011156103c357600854905061088c565b6000546002886040516103d69190610e7e565b908152602001604051809103902060000180549050106103fa57600954905061088c565b6000821415610480576002876040516104139190610e7e565b908152602001604051809103902060030160009054906101000a900460ff161561044157600c54905061088c565b60016002886040516104539190610e7e565b908152602001604051809103902060030160006101000a81548160ff021916908315150217905550610781565b60018214156107675760015460028660405161049c9190610e7e565b908152602001604051809103902060020154106104bd57600a54905061088c565b60006003856040516104cf9190610e7e565b908152602001604051809103902060010154141561054457846003856040516104f89190610e7e565b9081526020016040518091039020600001908051906020019061051c9291906109a9565b508260038560405161052e9190610e7e565b9081526020016040518091039020600101819055505b6003846040516105549190610e7e565b9081526020016040518091039020600101546003856040516105769190610e7e565b9081526020016040518091039020600201541061059757600b54905061088c565b600084886040516020016105ac929190610e95565b604051602081830303815290604052805190602001209050600115156005600083815260200190815260200160002060009054906101000a900460ff16151514156105fc57600d5491505061088c565b846004600089815260200190815260200160002060010190805190602001906106269291906109a9565b506002866040516106379190610e7e565b908152602001604051809103902060020160008154600101919050819055506003856040516106669190610e7e565b9081526020016040518091039020600201600081546001019190508190555060016005600083815260200190815260200160002060006101000a81548160ff0219169083151502179055507f96dff299e9c393b410a3143f0447c7b1c3f021468d248a04ef3e907483cb0ad56002876040516106e29190610e7e565b9081526020016040518091039020600201546040516107019190610fa1565b60405180910390a17fda2b777ecf22957bf55f40fb6e1d664c625e3ebee1a16aedb137468089f4d88660038660405161073a9190610e7e565b9081526020016040518091039020600201546040516107599190610fa1565b60405180910390a150610780565b60028214156107755761077f565b600e54905061088c565b5b5b866004600088815260200190815260200160002060000190805190602001906107ab9291906109a9565b506002876040516107bc9190610e7e565b90815260200160405180910390206000018690806001815401808255809150506001900390600052602060002001600090919091909150557fe710cf5e148cda86a54b02cb2eef7b644b5eddd3861e6924e334bb9afe327fac6002886040516108259190610e7e565b9081526020016040518091039020600001805490506040516108479190610fa1565b60405180910390a17fa3c54e9dd9cd0b7a4980113927b8db85658a24c25b6dcae02c25f7505c6f787f8660405161087e9190610f48565b60405180910390a160065490505b9695505050505050565b60606000806003846040516108ab9190610e7e565b90815260200160405180910390206000016003856040516108cc9190610e7e565b9081526020016040518091039020600101546003866040516108ee9190610e7e565b908152602001604051809103902060020154828054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156109955780601f1061096a57610100808354040283529160200191610995565b820191906000526020600020905b81548152906001019060200180831161097857829003601f168201915b505050505092509250925092509193909250565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106109ea57805160ff1916838001178555610a18565b82800160010185558215610a18579182015b82811115610a175782518255916020019190600101906109fc565b5b509050610a259190610a29565b5090565b610a4b91905b80821115610a47576000816000905550600101610a2f565b5090565b90565b600081359050610a5d81611176565b92915050565b600081519050610a7281611176565b92915050565b600082601f830112610a8957600080fd5b8135610a9c610a9782610fe9565b610fbc565b91508082526020830160208301858383011115610ab857600080fd5b610ac3838284611123565b50505092915050565b600081359050610adb8161118d565b92915050565b600060208284031215610af357600080fd5b6000610b0184828501610a63565b91505092915050565b600060208284031215610b1c57600080fd5b600082013567ffffffffffffffff811115610b3657600080fd5b610b4284828501610a78565b91505092915050565b60008060008060008060c08789031215610b6457600080fd5b600087013567ffffffffffffffff811115610b7e57600080fd5b610b8a89828a01610a78565b9650506020610b9b89828a01610a4e565b955050604087013567ffffffffffffffff811115610bb857600080fd5b610bc489828a01610a78565b945050606087013567ffffffffffffffff811115610be157600080fd5b610bed89828a01610a78565b9350506080610bfe89828a01610acc565b92505060a0610c0f89828a01610a4e565b9150509295509295509295565b6000610c288383610d39565b60208301905092915050565b6000610c408383610d66565b905092915050565b610c51816110bf565b82525050565b6000610c6282611035565b610c6c8185611070565b9350610c7783611015565b8060005b83811015610ca8578151610c8f8882610c1c565b9750610c9a83611056565b925050600181019050610c7b565b5085935050505092915050565b6000610cc082611040565b610cca8185611081565b935083602082028501610cdc85611025565b8060005b85811015610d185784840389528151610cf98582610c34565b9450610d0483611063565b925060208a01995050600181019050610ce0565b50829750879550505050505092915050565b610d33816110d1565b82525050565b610d42816110dd565b82525050565b610d51816110dd565b82525050565b610d6081611111565b82525050565b6000610d718261104b565b610d7b8185611092565b9350610d8b818560208601611132565b610d9481611165565b840191505092915050565b6000610daa8261104b565b610db481856110a3565b9350610dc4818560208601611132565b610dcd81611165565b840191505092915050565b6000610de38261104b565b610ded81856110b4565b9350610dfd818560208601611132565b80840191505092915050565b6000610e166036836110a3565b91507f7472616465546967657228737472696e672c696e743235362c737472696e672c60008301527f737472696e672c75696e743235362c696e7432353629000000000000000000006020830152604082019050919050565b610e7881611107565b82525050565b6000610e8a8284610dd8565b915081905092915050565b6000610ea18285610dd8565b9150610ead8284610dd8565b9150",
        "8190509392505050565b6000606082019050610ece6000830185610c48565b8181036020830152610edf81610e09565b9050610eee6040830184610d57565b9392505050565b60006080820190508181036000830152610f0f8187610c57565b90508181036020830152610f238186610cb5565b9050610f326040830185610e6f565b610f3f6060830184610d2a565b95945050505050565b6000602082019050610f5d6000830184610d48565b92915050565b60006060820190508181036000830152610f7d8186610d9f565b9050610f8c6020830185610e6f565b610f996040830184610e6f565b949350505050565b6000602082019050610fb66000830184610e6f565b92915050565b6000604051905081810181811067ffffffffffffffff82111715610fdf57600080fd5b8060405250919050565b600067ffffffffffffffff82111561100057600080fd5b601f19601f8301169050602081019050919050565b6000819050602082019050919050565b6000819050602082019050919050565b600081519050919050565b600081519050919050565b600081519050919050565b6000602082019050919050565b6000602082019050919050565b600082825260208201905092915050565b600082825260208201905092915050565b600082825260208201905092915050565b600082825260208201905092915050565b600081905092915050565b60006110ca826110e7565b9050919050565b60008115159050919050565b6000819050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b600061111c82611107565b9050919050565b82818337600083830152505050565b60005b83811015611150578082015181840152602081019050611135565b8381111561115f576000848401525b50505050565b6000601f19601f8301169050919050565b61117f816110dd565b811461118a57600080fd5b50565b61119681611107565b81146111a157600080fd5b5056fea264697066735822122094243b51865a7f55a58b0a7e96e856475eda1f90bdd070201913aef68be34a7e64736f6c634300060a0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"cardSendedLog\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"fromUserSendedLog\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"int256\",\"name\":\"\",\"type\":\"int256\"}],\"name\":\"tigerIDReceivedLog\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"toUserOwnedLog\",\"type\":\"event\"},{\"inputs\":[],\"name\":\"enableParallel\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"cardID\",\"type\":\"string\"}],\"name\":\"getCard\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"},{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"openID\",\"type\":\"string\"}],\"name\":\"getUser\",\"outputs\":[{\"internalType\":\"int256[]\",\"name\":\"\",\"type\":\"int256[]\"},{\"internalType\":\"string[]\",\"name\":\"\",\"type\":\"string[]\"},{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"},{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"toOpenID\",\"type\":\"string\"},{\"internalType\":\"int256\",\"name\":\"tigerID\",\"type\":\"int256\"},{\"internalType\":\"string\",\"name\":\"fromOpenID\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"cardID\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"cardLimit\",\"type\":\"uint256\"},{\"internalType\":\"int256\",\"name\":\"tradeType\",\"type\":\"int256\"}],\"name\":\"tradeTiger\",\"outputs\":[{\"internalType\":\"int256\",\"name\":\"\",\"type\":\"int256\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_ENABLEPARALLEL = "enableParallel";

    public static final String FUNC_GETCARD = "getCard";

    public static final String FUNC_GETUSER = "getUser";

    public static final String FUNC_TRADETIGER = "tradeTiger";

    public static final Event CARDSENDEDLOG_EVENT =
            new Event(
                    "cardSendedLog",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));;

    public static final Event FROMUSERSENDEDLOG_EVENT =
            new Event(
                    "fromUserSendedLog",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));;

    public static final Event TIGERIDRECEIVEDLOG_EVENT =
            new Event(
                    "tigerIDReceivedLog",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));;

    public static final Event TOUSEROWNEDLOG_EVENT =
            new Event(
                    "toUserOwnedLog",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));;

    protected TigerHole(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public List<CardSendedLogEventResponse> getCardSendedLogEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList =
                extractEventParametersWithLog(CARDSENDEDLOG_EVENT, transactionReceipt);
        ArrayList<CardSendedLogEventResponse> responses =
                new ArrayList<CardSendedLogEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CardSendedLogEventResponse typedResponse = new CardSendedLogEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.cardSendedLogParam0 =
                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeCardSendedLogEvent(
            String fromBlock, String toBlock, List<String> otherTopics, EventCallback callback) {
        String topic0 = eventEncoder.encode(CARDSENDEDLOG_EVENT);
        subscribeEvent(ABI, BINARY, topic0, fromBlock, toBlock, otherTopics, callback);
    }

    public void subscribeCardSendedLogEvent(EventCallback callback) {
        String topic0 = eventEncoder.encode(CARDSENDEDLOG_EVENT);
        subscribeEvent(ABI, BINARY, topic0, callback);
    }

    public List<FromUserSendedLogEventResponse> getFromUserSendedLogEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList =
                extractEventParametersWithLog(FROMUSERSENDEDLOG_EVENT, transactionReceipt);
        ArrayList<FromUserSendedLogEventResponse> responses =
                new ArrayList<FromUserSendedLogEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            FromUserSendedLogEventResponse typedResponse = new FromUserSendedLogEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.fromUserSendedLogParam0 =
                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeFromUserSendedLogEvent(
            String fromBlock, String toBlock, List<String> otherTopics, EventCallback callback) {
        String topic0 = eventEncoder.encode(FROMUSERSENDEDLOG_EVENT);
        subscribeEvent(ABI, BINARY, topic0, fromBlock, toBlock, otherTopics, callback);
    }

    public void subscribeFromUserSendedLogEvent(EventCallback callback) {
        String topic0 = eventEncoder.encode(FROMUSERSENDEDLOG_EVENT);
        subscribeEvent(ABI, BINARY, topic0, callback);
    }

    public List<TigerIDReceivedLogEventResponse> getTigerIDReceivedLogEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList =
                extractEventParametersWithLog(TIGERIDRECEIVEDLOG_EVENT, transactionReceipt);
        ArrayList<TigerIDReceivedLogEventResponse> responses =
                new ArrayList<TigerIDReceivedLogEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TigerIDReceivedLogEventResponse typedResponse = new TigerIDReceivedLogEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.tigerIDReceivedLogParam0 =
                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeTigerIDReceivedLogEvent(
            String fromBlock, String toBlock, List<String> otherTopics, EventCallback callback) {
        String topic0 = eventEncoder.encode(TIGERIDRECEIVEDLOG_EVENT);
        subscribeEvent(ABI, BINARY, topic0, fromBlock, toBlock, otherTopics, callback);
    }

    public void subscribeTigerIDReceivedLogEvent(EventCallback callback) {
        String topic0 = eventEncoder.encode(TIGERIDRECEIVEDLOG_EVENT);
        subscribeEvent(ABI, BINARY, topic0, callback);
    }

    public List<ToUserOwnedLogEventResponse> getToUserOwnedLogEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList =
                extractEventParametersWithLog(TOUSEROWNEDLOG_EVENT, transactionReceipt);
        ArrayList<ToUserOwnedLogEventResponse> responses =
                new ArrayList<ToUserOwnedLogEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ToUserOwnedLogEventResponse typedResponse = new ToUserOwnedLogEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.toUserOwnedLogParam0 =
                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeToUserOwnedLogEvent(
            String fromBlock, String toBlock, List<String> otherTopics, EventCallback callback) {
        String topic0 = eventEncoder.encode(TOUSEROWNEDLOG_EVENT);
        subscribeEvent(ABI, BINARY, topic0, fromBlock, toBlock, otherTopics, callback);
    }

    public void subscribeToUserOwnedLogEvent(EventCallback callback) {
        String topic0 = eventEncoder.encode(TOUSEROWNEDLOG_EVENT);
        subscribeEvent(ABI, BINARY, topic0, callback);
    }

    public TransactionReceipt enableParallel() {
        final Function function =
                new Function(
                        FUNC_ENABLEPARALLEL,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] enableParallel(TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_ENABLEPARALLEL,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForEnableParallel() {
        final Function function =
                new Function(
                        FUNC_ENABLEPARALLEL,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple3<String, BigInteger, BigInteger> getCard(String cardID) throws ContractException {
        final Function function =
                new Function(
                        FUNC_GETCARD,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(cardID)),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Uint256>() {},
                                new TypeReference<Uint256>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple3<String, BigInteger, BigInteger>(
                (String) results.get(0).getValue(),
                (BigInteger) results.get(1).getValue(),
                (BigInteger) results.get(2).getValue());
    }

    public Tuple4<List<BigInteger>, List<String>, BigInteger, Boolean> getUser(String openID)
            throws ContractException {
        final Function function =
                new Function(
                        FUNC_GETUSER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(openID)),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<DynamicArray<Int256>>() {},
                                new TypeReference<DynamicArray<Utf8String>>() {},
                                new TypeReference<Uint256>() {},
                                new TypeReference<Bool>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple4<List<BigInteger>, List<String>, BigInteger, Boolean>(
                convertToNative((List<Int256>) results.get(0).getValue()),
                convertToNative((List<Utf8String>) results.get(1).getValue()),
                (BigInteger) results.get(2).getValue(),
                (Boolean) results.get(3).getValue());
    }

    public TransactionReceipt tradeTiger(
            String toOpenID,
            BigInteger tigerID,
            String fromOpenID,
            String cardID,
            BigInteger cardLimit,
            BigInteger tradeType) {
        final Function function =
                new Function(
                        FUNC_TRADETIGER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(toOpenID),
                                new org.fisco.bcos.sdk.abi.datatypes.generated.Int256(tigerID),
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(fromOpenID),
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(cardID),
                                new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(cardLimit),
                                new org.fisco.bcos.sdk.abi.datatypes.generated.Int256(tradeType)),
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] tradeTiger(
            String toOpenID,
            BigInteger tigerID,
            String fromOpenID,
            String cardID,
            BigInteger cardLimit,
            BigInteger tradeType,
            TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_TRADETIGER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(toOpenID),
                                new org.fisco.bcos.sdk.abi.datatypes.generated.Int256(tigerID),
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(fromOpenID),
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(cardID),
                                new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(cardLimit),
                                new org.fisco.bcos.sdk.abi.datatypes.generated.Int256(tradeType)),
                        Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForTradeTiger(
            String toOpenID,
            BigInteger tigerID,
            String fromOpenID,
            String cardID,
            BigInteger cardLimit,
            BigInteger tradeType) {
        final Function function =
                new Function(
                        FUNC_TRADETIGER,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(toOpenID),
                                new org.fisco.bcos.sdk.abi.datatypes.generated.Int256(tigerID),
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(fromOpenID),
                                new org.fisco.bcos.sdk.abi.datatypes.Utf8String(cardID),
                                new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(cardLimit),
                                new org.fisco.bcos.sdk.abi.datatypes.generated.Int256(tradeType)),
                        Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple6<String, BigInteger, String, String, BigInteger, BigInteger> getTradeTigerInput(
            TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_TRADETIGER,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Int256>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Uint256>() {},
                                new TypeReference<Int256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple6<String, BigInteger, String, String, BigInteger, BigInteger>(
                (String) results.get(0).getValue(),
                (BigInteger) results.get(1).getValue(),
                (String) results.get(2).getValue(),
                (String) results.get(3).getValue(),
                (BigInteger) results.get(4).getValue(),
                (BigInteger) results.get(5).getValue());
    }

    public Tuple1<BigInteger> getTradeTigerOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_TRADETIGER,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>((BigInteger) results.get(0).getValue());
    }

    public static TigerHole load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new TigerHole(contractAddress, client, credential);
    }

    public static TigerHole deploy(Client client, CryptoKeyPair credential)
            throws ContractException {
        return deploy(TigerHole.class, client, credential, getBinary(client.getCryptoSuite()), "");
    }

    public static class CardSendedLogEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger cardSendedLogParam0;
    }

    public static class FromUserSendedLogEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger fromUserSendedLogParam0;
    }

    public static class TigerIDReceivedLogEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger tigerIDReceivedLogParam0;
    }

    public static class ToUserOwnedLogEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger toUserOwnedLogParam0;
    }
}
