package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.DynamicArray;
import org.fisco.bcos.sdk.v3.codec.datatypes.Event;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int32;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class TableTest extends Contract {
    public static final String[] BINARY_ARRAY = {
        "60806040523480156200001157600080fd5b5060408051808201825260068152651d17dd195cdd60d21b6020820152905163f23f63c960e01b81526000916110029163f23f63c991620000559160040162000385565b602060405180830381865afa15801562000073573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190620000999190620003a1565b90506001600160a01b03811615620000d157600080546001600160a01b0319166001600160a01b0392909216919091179055620004a4565b60408051600280825260608201909252600091816020015b6060815260200190600190039081620000e9579050509050604051806040016040528060048152602001636e616d6560e01b81525081600081518110620001345762000134620003cc565b60200260200101819052506040518060400160405280600381526020016261676560e81b81525081600181518110620001715762000171620003cc565b602090810291909101810191909152604080516080810182526002818301908152611a5960f21b606083015281528083018490528151808301835260068152651d17dd195cdd60d21b9381019390935290516318d2d28f60e11b81529091600091611002916331a5a51e91620001ed91908690600401620003e2565b6020604051808303816000875af11580156200020d573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906200023391906200047f565b90508060030b600014620002825760405162461bcd60e51b815260206004820152601260248201527118dc99585d19481d18589b194819985a5b1960721b604482015260640160405180910390fd5b60408051808201825260068152651d17dd195cdd60d21b6020820152905163f23f63c960e01b81526000916110029163f23f63c991620002c59160040162000385565b602060405180830381865afa158015620002e3573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190620003099190620003a1565b600080546001600160a01b0319166001600160a01b039290921691909117905550620004a49350505050565b6000815180845260005b818110156200035d576020818501810151868301820152016200033f565b8181111562000370576000602083870101525b50601f01601f19169290920160200192915050565b6020815260006200039a602083018462000335565b9392505050565b600060208284031215620003b457600080fd5b81516001600160a01b03811681146200039a57600080fd5b634e487b7160e01b600052603260045260246000fd5b604081526000620003f7604083018562000335565b60208382038185015284516040835262000415604084018262000335565b9050818601518382038385015281935080518083528383019450838160051b840101848301925060005b828110156200047157601f198583030187526200045e82855162000335565b968601969386019391506001016200043f565b509998505050505050505050565b6000602082840312156200049257600080fd5b81518060030b81146200039a57600080fd5b610dcb80620004b46000396000f3fe608060405234801561001057600080fd5b50600436106100625760003560e01c80632fe99bdc1461006757806331c3e4561461009257806355f150f1146100a55780636a5bae4e146100bb57806380599e4b146100dc578063fcd7e3c1146100ef575b600080fd5b61007a6100753660046107c5565b610110565b60405160039190910b81526020015b60405180910390f35b61007a6100a03660046107c5565b61024d565b6100ad6103e0565b6040516100899291906108a9565b6100ce6100c936600461093f565b61047d565b604051908152602001610089565b61007a6100ea366004610a37565b610545565b6101026100fd366004610a37565b6105f9565b604051610089929190610a74565b60408051600280825260608201909252600091829190816020015b606081526020019060019003908161012b579050509050838160008151811061015657610156610aa2565b6020026020010181905250828160018151811061017557610175610aa2565b602090810291909101810191909152604080518082018252878152918201839052600080549151635c6e105f60e01b815290916001600160a01b031690635c6e105f906101c6908590600401610b32565b6020604051808303816000875af11580156101e5573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906102099190610b4c565b604051600382900b81529091507fc57b01fa77f41df77eaab79a0e2623fab2e7ae3e9530d9b1cab225ad65f2b7ce9060200160405180910390a19695505050505050565b60408051600280825260608201909252600091829190816020015b6040805180820190915260608082526020820152815260200190600190039081610268575050604080516080810182526004918101918252636e616d6560e01b60608201529081526020810186905281519192509082906000906102ce576102ce610aa2565b602002602001018190525060405180604001604052806040518060400160405280600381526020016261676560e81b8152508152602001848152508160018151811061031c5761031c610aa2565b6020908102919091010152600080546040516341ffd75f60e01b81526001600160a01b03909116906341ffd75f9061035a9089908690600401610b6f565b6020604051808303816000875af1158015610379573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061039d9190610b4c565b604051600382900b81529091507f8e5890af40fc24a059396aca2f83d6ce41fcef086876548fa4fb8ec27e9d292a9060200160405180910390a195945050505050565b60408051808201825260068152651d17dd195cdd60d21b602082015290516317435b5560e21b8152606091829160009161100291635d0d6d549161042691600401610c04565b600060405180830381865afa158015610443573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f1916820160405261046b9190810190610d3b565b80516020909101519094909350915050565b6040805180820182528381526020810183905290516318d2d28f60e11b8152600091908290611002906331a5a51e906104bc9089908690600401610d70565b6020604051808303816000875af11580156104db573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906104ff9190610b4c565b604051600382900b81529091507fb5636cd912a73dcdb5b570dbe331dfa3e6435c93e029e642def2c8e40dacf2109060200160405180910390a160030b95945050505050565b600080546040516380599e4b60e01b815282916001600160a01b0316906380599e4b90610576908690600401610c04565b6020604051808303816000875af1158015610595573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906105b99190610b4c565b604051600382900b81529091507f4b930e280fe29620bdff00c88155d46d6d82a39f45dd5c3ea114dc31573581129060200160405180910390a192915050565b6000805460405163fcd7e3c160e01b8152606092839290916001600160a01b039091169063fcd7e3c190610631908790600401610c04565b600060405180830381865afa15801561064e573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f191682016040526106769190810190610d3b565b9050606080826020015151600214156106cc5782602001516000815181106106a0576106a0610aa2565b6020026020010151915082602001516001815181106106c1576106c1610aa2565b602002602001015190505b909590945092505050565b634e487b7160e01b600052604160045260246000fd5b6040805190810167ffffffffffffffff81118282101715610710576107106106d7565b60405290565b604051601f8201601f1916810167ffffffffffffffff8111828210171561073f5761073f6106d7565b604052919050565b600067ffffffffffffffff821115610761576107616106d7565b50601f01601f191660200190565b600082601f83011261078057600080fd5b813561079361078e82610747565b610716565b8181528460208386010111156107a857600080fd5b816020850160208301376000918101602001919091529392505050565b6000806000606084860312156107da57600080fd5b833567ffffffffffffffff808211156107f257600080fd5b6107fe8783880161076f565b9450602086013591508082111561081457600080fd5b6108208783880161076f565b9350604086013591508082111561083657600080fd5b506108438682870161076f565b9150509250925092565b60005b83811015610868578181015183820152602001610850565b83811115610877576000848401525b50505050565b6000815180845261089581602086016020860161084d565b601f01601f19169290920160200192915050565b6040815260006108bc604083018561087d565b6020838203818501528185518084528284019150828160051b85010183880160005b8381101561090c57601f198784030185526108fa83835161087d565b948601949250908501906001016108de565b50909998505050505050505050565b600067ffffffffffffffff821115610935576109356106d7565b5060051b60200190565b60008060006060848603121561095457600080fd5b833567ffffffffffffffff8082111561096c57600080fd5b6109788783880161076f565b945060209150818601358181111561098f57600080fd5b61099b8882890161076f565b9450506040860135818111156109b057600080fd5b8601601f810188136109c157600080fd5b80356109cf61078e8261091b565b81815260059190911b8201840190848101908a8311156109ee57600080fd5b8584015b83811015610a2657803586811115610a0a5760008081fd5b610a188d898389010161076f565b8452509186019186016109f2565b508096505050505050509250925092565b600060208284031215610a4957600080fd5b813567ffffffffffffffff811115610a6057600080fd5b610a6c8482850161076f565b949350505050565b604081526000610a87604083018561087d565b8281036020840152610a99818561087d565b95945050505050565b634e487b7160e01b600052603260045260246000fd5b6000815160408452610acd604085018261087d565b9050602080840151858303828701528281518085528385019150838160051b860101848401935060005b82811015610b2557601f19878303018452610b1382865161087d565b94860194938601939150600101610af7565b5098975050505050505050565b602081526000610b456020830184610ab8565b939250505056",
        "5b600060208284031215610b5e57600080fd5b81518060030b8114610b4557600080fd5b60006040808352610b828184018661087d565b6020848203818601528186518084528284019150828160051b85010183890160005b83811015610bf457868303601f1901855281518051898552610bc88a86018261087d565b91880151858303868a0152919050610be0818361087d565b968801969450505090850190600101610ba4565b50909a9950505050505050505050565b602081526000610b45602083018461087d565b600082601f830112610c2857600080fd5b8151610c3661078e82610747565b818152846020838601011115610c4b57600080fd5b610a6c82602083016020870161084d565b600060408284031215610c6e57600080fd5b610c766106ed565b9050815167ffffffffffffffff80821115610c9057600080fd5b610c9c85838601610c17565b8352602091508184015181811115610cb357600080fd5b8401601f81018613610cc457600080fd5b8051610cd261078e8261091b565b81815260059190911b82018401908481019088831115610cf157600080fd5b8584015b83811015610d2957805186811115610d0d5760008081fd5b610d1b8b8983890101610c17565b845250918601918601610cf5565b50808688015250505050505092915050565b600060208284031215610d4d57600080fd5b815167ffffffffffffffff811115610d6457600080fd5b610a6c84828501610c5c565b604081526000610d83604083018561087d565b8281036020840152610a998185610ab856fea2646970667358221220c2e1f3814c013d89b0a6671201a2a3c0916478bf6bccc97db6ffa76c045d775764736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "60806040523480156200001157600080fd5b5060408051808201825260068152651d17dd195cdd60d21b602082015290516359a48b6560e01b8152600091611002916359a48b6591620000559160040162000386565b602060405180830381865afa15801562000073573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190620000999190620003a2565b90506001600160a01b03811615620000d157600080546001600160a01b0319166001600160a01b0392909216919091179055620004a5565b60408051600280825260608201909252600091816020015b6060815260200190600190039081620000e9579050509050604051806040016040528060048152602001636e616d6560e01b81525081600081518110620001345762000134620003cd565b60200260200101819052506040518060400160405280600381526020016261676560e81b81525081600181518110620001715762000171620003cd565b602090810291909101810191909152604080516080810182526002818301908152611a5960f21b606083015281528083018490528151808301835260068152651d17dd195cdd60d21b93810193909352905163656db23160e11b815290916000916110029163cadb646291620001ed91908690600401620003e3565b6020604051808303816000875af11580156200020d573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019062000233919062000480565b90508060030b6000146200028357604051636381e58960e11b815260206004820152601260248201527118dc99585d19481d18589b194819985a5b1960721b604482015260640160405180910390fd5b60408051808201825260068152651d17dd195cdd60d21b602082015290516359a48b6560e01b8152600091611002916359a48b6591620002c69160040162000386565b602060405180830381865afa158015620002e4573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906200030a9190620003a2565b600080546001600160a01b0319166001600160a01b039290921691909117905550620004a59350505050565b6000815180845260005b818110156200035e5760208185018101518683018201520162000340565b8181111562000371576000602083870101525b50601f01601f19169290920160200192915050565b6020815260006200039b602083018462000336565b9392505050565b600060208284031215620003b557600080fd5b81516001600160a01b03811681146200039b57600080fd5b63b95aa35560e01b600052603260045260246000fd5b604081526000620003f8604083018562000336565b60208382038185015284516040835262000416604084018262000336565b9050818601518382038385015281935080518083528383019450838160051b840101848301925060005b828110156200047257601f198583030187526200045f82855162000336565b9686019693860193915060010162000440565b509998505050505050505050565b6000602082840312156200049357600080fd5b81518060030b81146200039b57600080fd5b610dca80620004b56000396000f3fe608060405234801561001057600080fd5b50600436106100625760003560e01c80631724a182146100675780635b325d7814610092578063753582a4146100b357806386b733f9146100c95780639b6ba099146100dc578063beb9d9ed146100ef575b600080fd5b61007a6100753660046107c4565b610110565b60405160039190910b81526020015b60405180910390f35b6100a56100a036600461084c565b61024d565b6040516100899291906108e5565b6100bb61032b565b604051610089929190610913565b61007a6100d736600461084c565b6103c8565b61007a6100ea3660046107c4565b61047c565b6101026100fd3660046109a9565b61060e565b604051908152602001610089565b60408051600280825260608201909252600091829190816020015b606081526020019060019003908161012b579050509050838160008151811061015657610156610aa1565b6020026020010181905250828160018151811061017557610175610aa1565b6020908102919091018101919091526040805180820182528781529182018390526000805491516304c8b95360e41b815290916001600160a01b031690634c8b9530906101c6908590600401610b31565b6020604051808303816000875af11580156101e5573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906102099190610b4b565b604051600382900b81529091507fdfc533ec2b52797a1229dc2495dbd3f4948f7c4c982ec077ad9d80810ec5c1f99060200160405180910390a19695505050505050565b60008054604051630b664baf60e31b8152606092839290916001600160a01b0390911690635b325d7890610285908790600401610b6e565b600060405180830381865afa1580156102a2573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f191682016040526102ca9190810190610ca5565b9050606080826020015151600214156103205782602001516000815181106102f4576102f4610aa1565b60200260200101519150826020015160018151811061031557610315610aa1565b602002602001015190505b909590945092505050565b60408051808201825260068152651d17dd195cdd60d21b60208201529051632e21756b60e21b815260609182916000916110029163b885d5ac9161037191600401610b6e565b600060405180830381865afa15801561038e573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f191682016040526103b69190810190610ca5565b80516020909101519094909350915050565b600080546040516386b733f960e01b815282916001600160a01b0316906386b733f9906103f9908690600401610b6e565b6020604051808303816000875af1158015610418573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061043c9190610b4b565b604051600382900b81529091507fe7769b56c2afa8e40381956f76b91d9ec19c34c0a81791702fdcae68e35a72719060200160405180910390a192915050565b60408051600280825260608201909252600091829190816020015b6040805180820190915260608082526020820152815260200190600190039081610497575050604080516080810182526004918101918252636e616d6560e01b60608201529081526020810186905281519192509082906000906104fd576104fd610aa1565b602002602001018190525060405180604001604052806040518060400160405280600381526020016261676560e81b8152508152602001848152508160018151811061054b5761054b610aa1565b602090810291909101015260008054604051627ea01360e21b81526001600160a01b03909116906301fa804c906105889089908690600401610cda565b6020604051808303816000875af11580156105a7573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906105cb9190610b4b565b604051600382900b81529091507fd72ab475a08df05fbd4f7f8cb4db1ad9dbdc26f54437fa6794acd97357779d2a9060200160405180910390a195945050505050565b60408051808201825283815260208101839052905163656db23160e11b81526000919082906110029063cadb64629061064d9089908690600401610d6f565b6020604051808303816000875af115801561066c573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906106909190610b4b565b604051600382900b81529091507f38411b2ef21b6826a8b9f48a1baa6b3388c3354ebdc5db749b35830ec581722d9060200160405180910390a160030b95945050505050565b63b95aa35560e01b600052604160045260246000fd5b6040805190810167ffffffffffffffff8111828210171561070f5761070f6106d6565b60405290565b604051601f8201601f1916810167ffffffffffffffff8111828210171561073e5761073e6106d6565b604052919050565b600067ffffffffffffffff821115610760576107606106d6565b50601f01601f191660200190565b600082601f83011261077f57600080fd5b813561079261078d82610746565b610715565b8181528460208386010111156107a757600080fd5b816020850160208301376000918101602001919091529392505050565b6000806000606084860312156107d957600080fd5b833567ffffffffffffffff808211156107f157600080fd5b6107fd8783880161076e565b9450602086013591508082111561081357600080fd5b61081f8783880161076e565b9350604086013591508082111561083557600080fd5b506108428682870161076e565b9150509250925092565b60006020828403121561085e57600080fd5b813567ffffffffffffffff81111561087557600080fd5b6108818482850161076e565b949350505050565b60005b838110156108a457818101518382015260200161088c565b838111156108b3576000848401525b50505050565b600081518084526108d1816020860160208601610889565b601f01601f19169290920160200192915050565b6040815260006108f860408301856108b9565b828103602084015261090a81856108b9565b95945050505050565b60408152600061092660408301856108b9565b6020838203818501528185518084528284019150828160051b85010183880160005b8381101561097657601f198784030185526109648383516108b9565b94860194925090850190600101610948565b50909998505050505050505050565b600067ffffffffffffffff82111561099f5761099f6106d6565b5060051b60200190565b6000806000606084860312156109be57600080fd5b833567ffffffffffffffff808211156109d657600080fd5b6109e28783880161076e565b94506020915081860135818111156109f957600080fd5b610a058882890161076e565b945050604086013581811115610a1a57600080fd5b8601601f81018813610a2b57600080fd5b8035610a3961078d82610985565b81815260059190911b8201840190848101908a831115610a5857600080fd5b8584015b83811015610a9057803586811115610a745760008081fd5b610a828d898389010161076e565b845250918601918601610a5c565b508096505050505050509250925092565b63b95aa35560e01b600052603260045260246000fd5b6000815160408452610acc60408501826108b9565b9050602080840151858303828701528281518085528385019150838160051b860101848401935060005b82811015610b2457601f19878303018452610b128286516108b9565b94860194938601939150600101610af6565b5098975050505050505050565b602081526000610b446020830184610ab7565b939250505056",
        "5b600060208284031215610b5d57600080fd5b81518060030b8114610b4457600080fd5b602081526000610b4460208301846108b9565b600082601f830112610b9257600080fd5b8151610ba061078d82610746565b818152846020838601011115610bb557600080fd5b610881826020830160208701610889565b600060408284031215610bd857600080fd5b610be06106ec565b9050815167ffffffffffffffff80821115610bfa57600080fd5b610c0685838601610b81565b8352602091508184015181811115610c1d57600080fd5b8401601f81018613610c2e57600080fd5b8051610c3c61078d82610985565b81815260059190911b82018401908481019088831115610c5b57600080fd5b8584015b83811015610c9357805186811115610c775760008081fd5b610c858b8983890101610b81565b845250918601918601610c5f565b50808688015250505050505092915050565b600060208284031215610cb757600080fd5b815167ffffffffffffffff811115610cce57600080fd5b61088184828501610bc6565b60006040808352610ced818401866108b9565b6020848203818601528186518084528284019150828160051b85010183890160005b83811015610d5f57868303601f1901855281518051898552610d338a8601826108b9565b91880151858303868a0152919050610d4b81836108b9565b968801969450505090850190600101610d0f565b50909a9950505050505050505050565b604081526000610d8260408301856108b9565b828103602084015261090a8185610ab756fea26469706673582212205e81964f2d4add8ce914a650ed2057132a634963c51ea44afd49056f648baff264736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"inputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"int256\",\"name\":\"count\",\"type\":\"int256\"}],\"name\":\"CreateResult\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"int256\",\"name\":\"count\",\"type\":\"int256\"}],\"name\":\"InsertResult\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"int256\",\"name\":\"count\",\"type\":\"int256\"}],\"name\":\"RemoveResult\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"int256\",\"name\":\"count\",\"type\":\"int256\"}],\"name\":\"UpdateResult\",\"type\":\"event\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"tableName\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"key\",\"type\":\"string\"},{\"internalType\":\"string[]\",\"name\":\"fields\",\"type\":\"string[]\"}],\"name\":\"createTable\",\"outputs\":[{\"internalType\":\"int256\",\"name\":\"\",\"type\":\"int256\"}],\"selector\":[1784393294,3199850989],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[],\"name\":\"desc\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"},{\"internalType\":\"string[]\",\"name\":\"\",\"type\":\"string[]\"}],\"selector\":[1441878257,1966441124],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"id\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"name\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"age\",\"type\":\"string\"}],\"name\":\"insert\",\"outputs\":[{\"internalType\":\"int32\",\"name\":\"\",\"type\":\"int32\"}],\"selector\":[803838940,388276610],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"id\",\"type\":\"string\"}],\"name\":\"remove\",\"outputs\":[{\"internalType\":\"int32\",\"name\":\"\",\"type\":\"int32\"}],\"selector\":[2153356875,2260153337],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"id\",\"type\":\"string\"}],\"name\":\"select\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"selector\":[4242006977,1530027384],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"id\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"name\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"age\",\"type\":\"string\"}],\"name\":\"update\",\"outputs\":[{\"internalType\":\"int32\",\"name\":\"\",\"type\":\"int32\"}],\"selector\":[834921558,2607521945],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_CREATETABLE = "createTable";

    public static final String FUNC_DESC = "desc";

    public static final String FUNC_INSERT = "insert";

    public static final String FUNC_REMOVE = "remove";

    public static final String FUNC_SELECT = "select";

    public static final String FUNC_UPDATE = "update";

    public static final Event CREATERESULT_EVENT =
            new Event(
                    "CreateResult",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));;

    public static final Event INSERTRESULT_EVENT =
            new Event(
                    "InsertResult",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));;

    public static final Event REMOVERESULT_EVENT =
            new Event(
                    "RemoveResult",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));;

    public static final Event UPDATERESULT_EVENT =
            new Event(
                    "UpdateResult",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));;

    protected TableTest(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public List<CreateResultEventResponse> getCreateResultEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList =
                extractEventParametersWithLog(CREATERESULT_EVENT, transactionReceipt);
        ArrayList<CreateResultEventResponse> responses =
                new ArrayList<CreateResultEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            CreateResultEventResponse typedResponse = new CreateResultEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.count = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public List<InsertResultEventResponse> getInsertResultEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList =
                extractEventParametersWithLog(INSERTRESULT_EVENT, transactionReceipt);
        ArrayList<InsertResultEventResponse> responses =
                new ArrayList<InsertResultEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            InsertResultEventResponse typedResponse = new InsertResultEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.count = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public List<RemoveResultEventResponse> getRemoveResultEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList =
                extractEventParametersWithLog(REMOVERESULT_EVENT, transactionReceipt);
        ArrayList<RemoveResultEventResponse> responses =
                new ArrayList<RemoveResultEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            RemoveResultEventResponse typedResponse = new RemoveResultEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.count = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public List<UpdateResultEventResponse> getUpdateResultEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList =
                extractEventParametersWithLog(UPDATERESULT_EVENT, transactionReceipt);
        ArrayList<UpdateResultEventResponse> responses =
                new ArrayList<UpdateResultEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            UpdateResultEventResponse typedResponse = new UpdateResultEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.count = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public TransactionReceipt createTable(String tableName, String key, List<String> fields) {
        final Function function =
                new Function(
                        FUNC_CREATETABLE,
                        Arrays.<Type>asList(
                                new Utf8String(tableName),
                                new Utf8String(key),
                                new DynamicArray<Utf8String>(
                                        Utf8String.class,
                                        org.fisco.bcos.sdk.v3.codec.Utils.typeMap(
                                                fields, Utf8String.class))),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String createTable(
            String tableName, String key, List<String> fields, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_CREATETABLE,
                        Arrays.<Type>asList(
                                new Utf8String(tableName),
                                new Utf8String(key),
                                new DynamicArray<Utf8String>(
                                        Utf8String.class,
                                        org.fisco.bcos.sdk.v3.codec.Utils.typeMap(
                                                fields, Utf8String.class))),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForCreateTable(
            String tableName, String key, List<String> fields) {
        final Function function =
                new Function(
                        FUNC_CREATETABLE,
                        Arrays.<Type>asList(
                                new Utf8String(tableName),
                                new Utf8String(key),
                                new DynamicArray<Utf8String>(
                                        Utf8String.class,
                                        org.fisco.bcos.sdk.v3.codec.Utils.typeMap(
                                                fields, Utf8String.class))),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple3<String, String, List<String>> getCreateTableInput(
            TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_CREATETABLE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<DynamicArray<Utf8String>>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple3<String, String, List<String>>(
                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                convertToNative((List<Utf8String>) results.get(2).getValue()));
    }

    public Tuple1<BigInteger> getCreateTableOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_CREATETABLE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>((BigInteger) results.get(0).getValue());
    }

    public Tuple2<String, List<String>> desc() throws ContractException {
        final Function function =
                new Function(
                        FUNC_DESC,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<DynamicArray<Utf8String>>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple2<String, List<String>>(
                (String) results.get(0).getValue(),
                convertToNative((List<Utf8String>) results.get(1).getValue()));
    }

    public TransactionReceipt insert(String id, String name, String age) {
        final Function function =
                new Function(
                        FUNC_INSERT,
                        Arrays.<Type>asList(
                                new Utf8String(id), new Utf8String(name), new Utf8String(age)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String insert(String id, String name, String age, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_INSERT,
                        Arrays.<Type>asList(
                                new Utf8String(id), new Utf8String(name), new Utf8String(age)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForInsert(String id, String name, String age) {
        final Function function =
                new Function(
                        FUNC_INSERT,
                        Arrays.<Type>asList(
                                new Utf8String(id), new Utf8String(name), new Utf8String(age)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple3<String, String, String> getInsertInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_INSERT,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple3<String, String, String>(
                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (String) results.get(2).getValue());
    }

    public Tuple1<BigInteger> getInsertOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_INSERT,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int32>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>((BigInteger) results.get(0).getValue());
    }

    public TransactionReceipt remove(String id) {
        final Function function =
                new Function(
                        FUNC_REMOVE,
                        Arrays.<Type>asList(new Utf8String(id)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String remove(String id, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_REMOVE,
                        Arrays.<Type>asList(new Utf8String(id)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForRemove(String id) {
        final Function function =
                new Function(
                        FUNC_REMOVE,
                        Arrays.<Type>asList(new Utf8String(id)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple1<String> getRemoveInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_REMOVE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>((String) results.get(0).getValue());
    }

    public Tuple1<BigInteger> getRemoveOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_REMOVE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int32>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>((BigInteger) results.get(0).getValue());
    }

    public Tuple2<String, String> select(String id) throws ContractException {
        final Function function =
                new Function(
                        FUNC_SELECT,
                        Arrays.<Type>asList(new Utf8String(id)),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple2<String, String>(
                (String) results.get(0).getValue(), (String) results.get(1).getValue());
    }

    public TransactionReceipt update(String id, String name, String age) {
        final Function function =
                new Function(
                        FUNC_UPDATE,
                        Arrays.<Type>asList(
                                new Utf8String(id), new Utf8String(name), new Utf8String(age)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String update(String id, String name, String age, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_UPDATE,
                        Arrays.<Type>asList(
                                new Utf8String(id), new Utf8String(name), new Utf8String(age)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForUpdate(String id, String name, String age) {
        final Function function =
                new Function(
                        FUNC_UPDATE,
                        Arrays.<Type>asList(
                                new Utf8String(id), new Utf8String(name), new Utf8String(age)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple3<String, String, String> getUpdateInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_UPDATE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple3<String, String, String>(
                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (String) results.get(2).getValue());
    }

    public Tuple1<BigInteger> getUpdateOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_UPDATE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int32>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>((BigInteger) results.get(0).getValue());
    }

    public static TableTest load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new TableTest(contractAddress, client, credential);
    }

    public static TableTest deploy(Client client, CryptoKeyPair credential)
            throws ContractException {
        return deploy(
                TableTest.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                getABI(),
                null,
                null);
    }

    public static class CreateResultEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger count;
    }

    public static class InsertResultEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger count;
    }

    public static class RemoveResultEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger count;
    }

    public static class UpdateResultEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger count;
    }
}
