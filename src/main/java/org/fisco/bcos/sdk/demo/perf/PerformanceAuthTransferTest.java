package org.fisco.bcos.sdk.demo.perf;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.Account;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.ContractCodecException;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.contract.auth.manager.AuthManager;
import org.fisco.bcos.sdk.v3.contract.auth.po.AuthType;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.v3.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class PerformanceAuthTransferTest {
    private static int DEFAULT_QPS_LIMIT = 1000;
    private static int addressPerContract;
    private static Client client;
    private static final Random random = new Random();
    private static List<String> contractList;
    private static Map<String, AtomicLong> accountLedger;
    private static Map<String, List<CryptoKeyPair>> contractAclMap;

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceAuthTransferTest test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceAuthTransferTest [groupId] [userCount] [contractCount] [txCount] [qps] [onlySetAcl].");
    }

    public static void main(String[] args)
            throws ContractException, IOException, InterruptedException {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl =
                    PerformanceAuthTransferTest.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                throw new IOException("The configFile " + configFileName + " doesn't exist!");
            }

            if (args.length < 5) {
                Usage();
                return;
            }
            String groupId = args[0];
            int userCount = Integer.parseInt(args[1]);
            int contractCount = Integer.parseInt(args[2]);
            int txCount = Integer.parseInt(args[3]);
            int qps = Integer.parseInt(args[4]);
            boolean isOnlySetAcl = false;
            if (args.length == 6) {
                isOnlySetAcl = Boolean.parseBoolean(args[5]);
                DEFAULT_QPS_LIMIT = isOnlySetAcl ? qps : DEFAULT_QPS_LIMIT;
            }

            accountLedger = new ConcurrentHashMap<>(contractCount);
            contractAclMap = new ConcurrentHashMap<>(contractCount);
            addressPerContract = userCount / contractCount;

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            random.setSeed(System.currentTimeMillis());
            client = sdk.getClient(groupId);
            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "PerformanceAuthTransferTest",
                            Runtime.getRuntime().availableProcessors());

            // new contracts, write to a list
            contractList = deployContracts(contractCount, threadPoolService);

            if (client.isAuthCheck()) {
                // set acl type to all new contract, random to white/black_list
                setContractAcl(threadPoolService);
                // setMethod acl to all new contract,
                // random to open/close, write it to a map
                setContractMethodAcl(threadPoolService);
            }
            // start send transaction
            if (!isOnlySetAcl) {
                start(txCount, qps, threadPoolService);
            }

            threadPoolService.getThreadPool().awaitTermination(0, TimeUnit.SECONDS);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static List<String> deployContracts(
            int contractCount, ThreadPoolService threadPoolService) throws InterruptedException {
        System.out.println("====== deployContracts, contractCount: " + contractCount);

        String[] contracts = new String[contractCount];
        String abi = Account.getABI();
        String binary = Account.getBinary(client.getCryptoSuite());
        AssembleTransactionProcessor assembleTransactionProcessor =
                TransactionProcessorFactory.createAssembleTransactionProcessor(
                        client, client.getCryptoSuite().getCryptoKeyPair(), "Account", abi, binary);
        RateLimiter limiter = RateLimiter.create(DEFAULT_QPS_LIMIT);

        System.out.println("Deploy contracts...");
        ProgressBar sentBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax(contractCount)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar receivedBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax(contractCount)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();

        CountDownLatch countDownLatch = new CountDownLatch(contractCount);
        for (int i = 0; i < contractCount; ++i) {
            List<CryptoKeyPair> cryptoKeyPairs = new ArrayList<>();
            for (int i1 = 0; i1 < addressPerContract; i1++) {
                cryptoKeyPairs.add(client.getCryptoSuite().getKeyPairFactory().generateKeyPair());
            }

            final int index = i;
            limiter.acquire();
            threadPoolService
                    .getThreadPool()
                    .execute(
                            () -> {
                                try {
                                    assembleTransactionProcessor.deployAsync(
                                            abi,
                                            binary,
                                            new ArrayList<>(),
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(TransactionReceipt receipt) {
                                                    long initBalance = Math.abs(random.nextLong());
                                                    contractAclMap.put(
                                                            receipt.getContractAddress(),
                                                            cryptoKeyPairs);
                                                    contracts[index] = receipt.getContractAddress();
                                                    try {
                                                        assembleTransactionProcessor
                                                                .sendTransactionAsync(
                                                                        receipt
                                                                                .getContractAddress(),
                                                                        abi,
                                                                        Account.FUNC_ADDBALANCE,
                                                                        Lists.newArrayList(
                                                                                BigInteger.valueOf(
                                                                                        initBalance)),
                                                                        new TransactionCallback() {
                                                                            @Override
                                                                            public void onResponse(
                                                                                    TransactionReceipt
                                                                                            receipt) {
                                                                                accountLedger.put(
                                                                                        receipt
                                                                                                .getContractAddress(),
                                                                                        new AtomicLong(
                                                                                                initBalance));
                                                                                countDownLatch
                                                                                        .countDown();
                                                                                receivedBar.step();
                                                                            }
                                                                        });
                                                    } catch (ContractCodecException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                    sentBar.step();
                                } catch (ContractCodecException e) {
                                    e.printStackTrace();
                                }
                            });
        }
        countDownLatch.await();
        sentBar.close();
        receivedBar.close();
        System.out.println("Create account finished!");

        return Arrays.asList(contracts);
    }

    private static void setContractAcl(ThreadPoolService threadPoolService)
            throws InterruptedException {
        System.out.println("====== setContractACL, contractCount: " + contractList.size());
        AuthManager authManager =
                new AuthManager(client, client.getCryptoSuite().getCryptoKeyPair());
        byte[] hash = client.getCryptoSuite().hash("addBalance(uint256)".getBytes());
        byte[] func = Arrays.copyOfRange(hash, 0, 4);
        RateLimiter limiter = RateLimiter.create(DEFAULT_QPS_LIMIT);

        ProgressBar sentBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax(contractList.size())
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar receivedBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax(contractList.size())
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();

        CountDownLatch countDownLatch = new CountDownLatch(contractList.size());
        Collector collector = new Collector();
        collector.setTotal(contractList.size());

        for (String account : contractList) {
            limiter.acquire();
            threadPoolService
                    .getThreadPool()
                    .execute(
                            () -> {
                                long now = System.currentTimeMillis();
                                authManager.asyncSetMethodAuthType(
                                        account,
                                        func,
                                        random.nextBoolean()
                                                ? AuthType.BLACK_LIST
                                                : AuthType.WHITE_LIST,
                                        retCode -> {
                                            long cost = System.currentTimeMillis() - now;
                                            collector.onPrecompiledMessage(retCode, cost);
                                            receivedBar.step();

                                            countDownLatch.countDown();
                                        });
                                sentBar.step();
                            });
        }
        countDownLatch.await();
        sentBar.close();
        receivedBar.close();
        collector.report();
        System.out.println("Set contract acl finished!");
    }

    private static void setContractMethodAcl(ThreadPoolService threadPoolService)
            throws InterruptedException {
        System.out.println(
                "====== setContractMethodAcl, contracts size: "
                        + contractList.size()
                        + ", account per contract: "
                        + addressPerContract);

        AuthManager authManager =
                new AuthManager(client, client.getCryptoSuite().getCryptoKeyPair());
        byte[] hash = client.getCryptoSuite().hash("addBalance(uint256)".getBytes());
        byte[] func = Arrays.copyOfRange(hash, 0, 4);
        RateLimiter limiter = RateLimiter.create(DEFAULT_QPS_LIMIT);

        ProgressBar sentBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax((long) contractList.size() * addressPerContract)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar receivedBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax((long) contractList.size() * addressPerContract)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();

        CountDownLatch countDownLatch =
                new CountDownLatch(contractList.size() * addressPerContract);

        for (String account : contractList) {
            for (CryptoKeyPair address : contractAclMap.get(account)) {
                limiter.acquire();
                threadPoolService
                        .getThreadPool()
                        .execute(
                                () -> {
                                    boolean isOpen = random.nextBoolean();
                                    authManager.asyncSetMethodAuth(
                                            account,
                                            func,
                                            address.getAddress(),
                                            isOpen,
                                            retCode -> {
                                                receivedBar.step();
                                                countDownLatch.countDown();
                                            });
                                    sentBar.step();
                                });
            }
        }
        countDownLatch.await();
        sentBar.close();
        receivedBar.close();
        System.out.println("Set contract acl method finished!");
    }

    public static void start(int count, int qps, ThreadPoolService threadPoolService)
            throws IOException, InterruptedException, ContractException {

        RateLimiter limiter = RateLimiter.create(qps);

        int sendContractCount = count / addressPerContract;

        System.out.println("Sending transactions...");
        ProgressBar sentBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax(count)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar receivedBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax(count)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();

        System.out.println(
                "====== Start send transaction test, tx count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", sendContractCount: "
                        + sendContractCount);
        CountDownLatch transactionLatch = new CountDownLatch(count);
        // prepare contract list

        List<Tuple2<AssembleTransactionProcessor, String>> assembleTransactionProcessors =
                new ArrayList<>(count);
        String binary = Account.getBinary(client.getCryptoSuite());
        String abi = Account.getABI();
        for (int i = 0; i < sendContractCount; i++) {
            final int index = i % contractList.size();
            String account = contractList.get(index);
            List<CryptoKeyPair> cryptoKeyPairs = contractAclMap.get(account);
            assert cryptoKeyPairs.size() == addressPerContract;
            for (CryptoKeyPair cryptoKeyPair : cryptoKeyPairs) {
                AssembleTransactionProcessor assembleTransactionProcessor =
                        new AssembleTransactionProcessor(
                                client,
                                cryptoKeyPair,
                                client.getGroup(),
                                client.getChainId(),
                                "Account",
                                abi,
                                binary);
                assembleTransactionProcessors.add(
                        new Tuple2<>(assembleTransactionProcessor, account));
            }
        }

        Collector collector = new Collector();
        collector.setTotal(count);

        for (int i = 0; i < count; i++) {
            Tuple2<AssembleTransactionProcessor, String> assembleTransactionProcessorTuple =
                    assembleTransactionProcessors.get(i);
            AssembleTransactionProcessor assembleTransactionProcessor =
                    assembleTransactionProcessorTuple.getValue1();
            String address = assembleTransactionProcessorTuple.getValue2();
            limiter.acquire();
            threadPoolService
                    .getThreadPool()
                    .execute(
                            () -> {
                                long now = System.currentTimeMillis();

                                final long value = Math.abs(random.nextLong() % 1000);
                                try {
                                    assembleTransactionProcessor.sendTransactionAsync(
                                            address,
                                            abi,
                                            Account.FUNC_ADDBALANCE,
                                            Lists.newArrayList(BigInteger.valueOf(value)),
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(TransactionReceipt receipt) {
                                                    long cost = System.currentTimeMillis() - now;
                                                    collector.onAuthCheckMessage(receipt, cost);
                                                    receivedBar.step();
                                                    transactionLatch.countDown();
                                                    if (receipt.isStatusOK()) {
                                                        accountLedger.get(address).addAndGet(value);
                                                    }
                                                }
                                            });
                                } catch (ContractCodecException e) {
                                    e.printStackTrace();
                                    receivedBar.step();
                                    transactionLatch.countDown();
                                }
                                sentBar.step();
                            });
        }
        transactionLatch.await();

        sentBar.close();
        receivedBar.close();
        collector.report();

        System.out.println("Sending transactions finished!");

        checkResult(threadPoolService);
    }

    private static void checkResult(ThreadPoolService threadPoolService)
            throws InterruptedException {
        System.out.println("Checking result...");
        CountDownLatch checkLatch = new CountDownLatch(accountLedger.size());
        for (Map.Entry<String, AtomicLong> entry : accountLedger.entrySet()) {
            final String accountAddress = entry.getKey();
            final long expectBalance = entry.getValue().longValue();
            if (accountAddress == null || accountAddress.isEmpty()) {
                checkLatch.countDown();
                continue;
            }
            threadPoolService
                    .getThreadPool()
                    .execute(
                            () -> {
                                try {
                                    Account account =
                                            Account.load(
                                                    accountAddress,
                                                    client,
                                                    client.getCryptoSuite().getCryptoKeyPair());
                                    BigInteger balance = account.balance();
                                    if (balance.longValue() != expectBalance) {
                                        System.out.println(
                                                "Check failed! Account["
                                                        + accountAddress
                                                        + "] balance: "
                                                        + balance
                                                        + " not equal to expected: "
                                                        + expectBalance);
                                    }
                                    checkLatch.countDown();
                                } catch (ContractException e) {
                                    checkLatch.countDown();
                                    e.printStackTrace();
                                }
                            });
        }
        checkLatch.await();
        System.out.println("Checking finished!");
    }
}
