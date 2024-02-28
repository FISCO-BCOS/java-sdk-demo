package org.fisco.bcos.sdk.demo.perf;

import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.BalanceBank;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.contract.precompiled.balance.BalanceService;
import org.fisco.bcos.sdk.v3.contract.precompiled.callback.PrecompiledCallback;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.ProxySignTransactionManager;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.TransferTransactionService;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.transaction.tools.Convert;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class PerformanceBalance {
    public static void usage() {
        System.out.println(" Usage:");
        System.out.println("====== PerformanceBalance test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceBalance [groupId] [eoaTransfer] [userCount] [total] [qps] [committeePath]");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceBalance [groupId] [contractTransfer] [userCount] [total] [qps] [committeePath]");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceBalance [groupId] [balancePrecompiledTransfer] [userCount] [total] [qps] [committeePath]");
    }

    public static void main(String[] args)
            throws ContractException, IOException, InterruptedException {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = PerformanceBalance.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }
            if (args.length < 6) {
                usage();
                return;
            }
            String groupId = args[0];
            String command = args[1];
            int userCount = Integer.parseInt(args[2]);
            int total = Integer.parseInt(args[3]);
            int qps = Integer.parseInt(args[4]);
            String committeePath = args[5];

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            Client client = sdk.getClient(groupId);
            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "PerformanceBalance", Runtime.getRuntime().availableProcessors());
            start(
                    groupId,
                    command,
                    userCount,
                    total,
                    qps,
                    committeePath,
                    threadPoolService,
                    client);
            threadPoolService.getThreadPool().awaitTermination(0, TimeUnit.SECONDS);
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            System.exit(-1);
        }
    }

    public static void start(
            String groupId,
            String command,
            int userCount,
            int total,
            int qps,
            String committeePath,
            ThreadPoolService threadPoolService,
            Client client)
            throws ContractException, IOException, InterruptedException {
        System.out.println(
                "====== PerformanceBalance "
                        + command
                        + ", userCount: "
                        + userCount
                        + ", total: "
                        + total
                        + ", qps: "
                        + qps
                        + ", groupId: "
                        + groupId
                        + ", committeePath: "
                        + committeePath);
        RateLimiter limiter = RateLimiter.create(qps);
        // get governor cryptoSuite
        CryptoSuite cryptoSuite = new CryptoSuite(client.getCryptoType());
        cryptoSuite.loadAccount("pem", committeePath, "");
        CryptoKeyPair committee = cryptoSuite.getCryptoKeyPair();
        BalanceService balanceService = new BalanceService(client, committee);
        System.out.println("get governor address: " + committee.getAddress());
        System.out.println(
                "clientUser address:" + client.getCryptoSuite().getCryptoKeyPair().getAddress());

        ProgressBar sendBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax(total)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar receiveBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax(total)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar errorBar =
                new ProgressBarBuilder()
                        .setTaskName("Error  :")
                        .setInitialMax(total)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();

        CountDownLatch transactionLatch = new CountDownLatch(total);
        AtomicLong totalCost = new AtomicLong(0);

        // switch command
        switch (command) {
            case "eoaTransfer":
                // create eoa accounts
                if (userCount < 2) {
                    System.out.println("userCount must be greater than 2");
                    return;
                }
                System.out.println("===1. create eoa accounts===");
                CryptoSuite[] cryptoSuites = new CryptoSuite[userCount];
                IntStream.range(0, userCount)
                        .parallel()
                        .forEach(i -> cryptoSuites[i] = new CryptoSuite(client.getCryptoType()));

                // init eoa accounts, use async
                System.out.println("===2. init eoa accounts===");
                for (int i = 0; i < userCount; i++) {
                    CompletableFuture<RetCode> retCodeCompletableFuture = new CompletableFuture<>();
                    balanceService.addBalanceAsync(
                            cryptoSuites[i].getCryptoKeyPair().getAddress(),
                            "100000000",
                            Convert.Unit.WEI,
                            retCode -> {
                                retCodeCompletableFuture.complete(retCode);
                            });
                    try {
                        RetCode retCode = retCodeCompletableFuture.get();
                        if (!(retCode.getCode() == 0)) {
                            System.out.println(
                                    "init eoa account failed, status: "
                                            + retCode.getCode()
                                            + ", message: "
                                            + retCode.getMessage());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // start eoa transfer
                ProxySignTransactionManager transactionManager =
                        new ProxySignTransactionManager(client);
                TransferTransactionService transferTransactionService =
                        new TransferTransactionService(transactionManager);
                System.out.println("===3. start eoa transfer===");
                Collector collector = new Collector();
                collector.setTotal(total);
                for (int i = 0; i < total; i++) {
                    limiter.acquire();
                    int finalJ = i % userCount;
                    threadPoolService
                            .getThreadPool()
                            .execute(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                long startTime = System.currentTimeMillis();
                                                Random random = new Random();
                                                int r = random.nextInt(100) + 1;
                                                transferTransactionService.asyncSendFunds(
                                                        cryptoSuites[finalJ],
                                                        cryptoSuites[
                                                                (userCount / 2 + finalJ)
                                                                        % userCount]
                                                                .getCryptoKeyPair()
                                                                .getAddress(),
                                                        BigDecimal.valueOf(r),
                                                        Convert.Unit.WEI,
                                                        new TransactionCallback() {
                                                            @Override
                                                            public void onResponse(
                                                                    TransactionReceipt receipt) {
                                                                long cost =
                                                                        System.currentTimeMillis()
                                                                                - startTime;
                                                                collector.onMessage(receipt, cost);
                                                                receiveBar.step();
                                                                if (!receipt.isStatusOK()) {
                                                                    errorBar.step();
                                                                    System.out.println(
                                                                            receipt.getStatus());
                                                                }
                                                                transactionLatch.countDown();
                                                                totalCost.addAndGet(
                                                                        System.currentTimeMillis()
                                                                                - startTime);
                                                            }
                                                        });
                                                sendBar.step();
                                            } catch (Exception e) {
                                                System.out.println("Exception: " + e);
                                                e.printStackTrace();
                                                errorBar.step();
                                                transactionLatch.countDown();
                                            }
                                        }
                                    });
                }
                // add result check
                transactionLatch.await();
                System.out.println("send transaction finished!");
                sendBar.close();
                receiveBar.close();
                errorBar.close();
                collector.report();
                System.out.println("=== verify user balance ===");
                List<BigInteger> balances =
                        IntStream.range(0, userCount)
                                .parallel()
                                .mapToObj(
                                        i -> {
                                            try {
                                                return balanceService.getBalance(
                                                        cryptoSuites[i]
                                                                .getCryptoKeyPair()
                                                                .getAddress());
                                            } catch (ContractException e) {
                                                e.printStackTrace();
                                            }
                                            return null;
                                        })
                                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                if (balances.stream()
                                .reduce(BigInteger::add)
                                .get()
                                .compareTo(BigInteger.valueOf(100000000L * userCount))
                        != 0) {
                    System.out.println(
                            "balance is not correct! get eoaUser totalBalance: "
                                    + balances.stream().reduce(BigInteger::add).get()
                                    + ", expected: "
                                    + BigInteger.valueOf(100000000L * userCount));
                } else {
                    System.out.println(
                            "Verify balance Done! eoaUser totalBalance: "
                                    + balances.stream().reduce(BigInteger::add).get()
                                    + ", expected: "
                                    + BigInteger.valueOf(100000000L * userCount));
                }
                break;

            case "contractTransfer":
                // create contract accounts
                System.out.println("===1. create contract accounts===");
                if (userCount % 10 != 0) {
                    System.out.println("userCount must be multiple of 10");
                    return;
                }
                List<String> contractsAddress = new ArrayList<>();

                List<CompletableFuture<List<String>>> allFutures = new ArrayList<>();

                int batchSize = 10;
                int numTasks = userCount / batchSize;

                for (int i = 0; i < numTasks; i++) {
                    int start = i * batchSize;
                    int end = Math.min((i + 1) * batchSize, userCount);

                    CompletableFuture<List<String>> future =
                            CompletableFuture.supplyAsync(
                                    () -> {
                                        List<String> addresses = new ArrayList<>();
                                        for (int j = start; j < end; j++) {
                                            try {
                                                BalanceBank balanceBank =
                                                        BalanceBank.deploy(
                                                                client,
                                                                client.getCryptoSuite()
                                                                        .getCryptoKeyPair());
                                                addresses.add(balanceBank.getContractAddress());
                                            } catch (ContractException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        return addresses;
                                    });
                    allFutures.add(future);
                }

                CompletableFuture<Void> allOf =
                        CompletableFuture.allOf(
                                allFutures.toArray(new CompletableFuture[allFutures.size()]));
                CompletableFuture<List<String>> allContracts =
                        allOf.thenApply(
                                v ->
                                        allFutures.stream()
                                                .flatMap(f -> f.join().stream())
                                                .collect(Collectors.toList()));

                contractsAddress.addAll(allContracts.join());

                // init contract balance
                System.out.println("===2. init contract balance===");
                for (int i = 0; i < userCount; i++) {
                    CompletableFuture<RetCode> retCodeCompletableFuture = new CompletableFuture<>();
                    balanceService.addBalanceAsync(
                            contractsAddress.get(i),
                            "100000000",
                            Convert.Unit.WEI,
                            retCode -> {
                                retCodeCompletableFuture.complete(retCode);
                            });
                    try {
                        RetCode retCode = retCodeCompletableFuture.get();
                        if (!(retCode.getCode() == 0)) {
                            System.out.println(
                                    "init contract account failed, status: "
                                            + retCode.getCode()
                                            + ", message: "
                                            + retCode.getMessage());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // start contract transfer
                System.out.println("===3. start contract transfer===");
                Collector collector1 = new Collector();
                collector1.setTotal(total);
                for (int i = 0; i < total; i++) {
                    limiter.acquire();
                    int finalJ = i % userCount;
                    BalanceBank balanceBank =
                            BalanceBank.load(
                                    contractsAddress.get(finalJ),
                                    client,
                                    client.getCryptoSuite().getCryptoKeyPair());
                    threadPoolService
                            .getThreadPool()
                            .execute(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                long startTime = System.currentTimeMillis();
                                                Random random = new Random();
                                                int r = random.nextInt(100) + 1;
                                                balanceBank.transfer(
                                                        contractsAddress.get(
                                                                (finalJ + 1) % userCount),
                                                        BigInteger.valueOf(r),
                                                        new TransactionCallback() {
                                                            @Override
                                                            public void onResponse(
                                                                    TransactionReceipt receipt) {
                                                                if (!receipt.isStatusOK()) {
                                                                    System.out.println(
                                                                            "transfer failed, receipt status:"
                                                                                    + receipt
                                                                                            .getStatus()
                                                                                    + ", receipt message:"
                                                                                    + receipt
                                                                                            .getMessage());
                                                                    errorBar.step();
                                                                }
                                                                long cost =
                                                                        System.currentTimeMillis()
                                                                                - startTime;
                                                                collector1.onMessage(receipt, cost);
                                                                receiveBar.step();
                                                                transactionLatch.countDown();
                                                                totalCost.addAndGet(cost);
                                                            }
                                                        });
                                                sendBar.step();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                errorBar.step();
                                                transactionLatch.countDown();
                                            }
                                        }
                                    });
                }
                transactionLatch.await();
                System.out.println("send transaction finished!");
                sendBar.close();
                receiveBar.close();
                errorBar.close();
                collector1.report();
                System.out.println("=== verify contract balance ===");
                List<BigInteger> contractBalances =
                        IntStream.range(0, userCount)
                                .parallel()
                                .mapToObj(
                                        i -> {
                                            try {
                                                return balanceService.getBalance(
                                                        contractsAddress.get(i));
                                            } catch (ContractException e) {
                                                e.printStackTrace();
                                            }
                                            return null;
                                        })
                                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                if (contractBalances.stream()
                                .reduce(BigInteger::add)
                                .get()
                                .compareTo(BigInteger.valueOf(100000000L * userCount))
                        != 0) {
                    System.out.println(
                            "Verify Balance failed! get contract totalBalance: "
                                    + contractBalances.stream().reduce(BigInteger::add).get()
                                    + ", expected: "
                                    + BigInteger.valueOf(100000000L * userCount));
                } else {
                    System.out.println(
                            "Verify Balance Success! eoaUser totalBalance: "
                                    + contractBalances.stream().reduce(BigInteger::add).get()
                                    + ", expected: "
                                    + BigInteger.valueOf(100000000L * userCount));
                }

                break;
            case "balancePrecompiledTransfer":
                // create contract accounts
                System.out.println("===1. create eoa accounts===");
                CryptoSuite[] eoaCryptoSuites = new CryptoSuite[userCount];
                IntStream.range(0, userCount)
                        .parallel()
                        .forEach(i -> eoaCryptoSuites[i] = new CryptoSuite(client.getCryptoType()));

                // init eoa accounts, use async
                System.out.println("===2. init eoa accounts===");
                for (int i = 0; i < userCount; i++) {
                    CompletableFuture<RetCode> retCodeCompletableFuture = new CompletableFuture<>();
                    balanceService.addBalanceAsync(
                            eoaCryptoSuites[i].getCryptoKeyPair().getAddress(),
                            "100000000",
                            Convert.Unit.WEI,
                            retCode -> {
                                retCodeCompletableFuture.complete(retCode);
                            });
                    try {
                        RetCode retCode = retCodeCompletableFuture.get();
                        if (!(retCode.getCode() == 0)) {
                            System.out.println(
                                    "init eoa account failed, status: "
                                            + retCode.getCode()
                                            + ", message: "
                                            + retCode.getMessage());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // PerformanceBalancePrecompiled Test
                System.out.println("===3. PerformanceBalancePrecompiled Test===");
                Collector collector2 = new Collector();
                collector2.setTotal(total);
                for (int i = 0; i < total; i++) {
                    limiter.acquire();
                    int finalJ = i % userCount;
                    threadPoolService
                            .getThreadPool()
                            .execute(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                long startTime = System.currentTimeMillis();
                                                Random random = new Random();
                                                int r = random.nextInt(100) + 1;
                                                balanceService.transferAsync(
                                                        eoaCryptoSuites[finalJ]
                                                                .getCryptoKeyPair()
                                                                .getAddress(),
                                                        eoaCryptoSuites[(finalJ + 1) % userCount]
                                                                .getCryptoKeyPair()
                                                                .getAddress(),
                                                        String.valueOf(r),
                                                        Convert.Unit.WEI,
                                                        new PrecompiledCallback() {
                                                            @Override
                                                            public void onResponse(
                                                                    RetCode retCode) {
                                                                if (!(retCode.getCode() == 0)) {
                                                                    System.out.println(
                                                                            "transfer failed, status: "
                                                                                    + retCode
                                                                                            .getCode()
                                                                                    + ", message: "
                                                                                    + retCode
                                                                                            .getMessage());
                                                                    errorBar.step();
                                                                }
                                                                long cost =
                                                                        System.currentTimeMillis()
                                                                                - startTime;
                                                                collector2.onMessage(null, cost);
                                                                receiveBar.step();
                                                                transactionLatch.countDown();
                                                                totalCost.addAndGet(cost);
                                                            }
                                                        });
                                                sendBar.step();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                }
                transactionLatch.await();
                System.out.println("send transaction finished!");
                sendBar.close();
                receiveBar.close();
                errorBar.close();
                collector2.report();

                System.out.println("=== verify contract balance ===");
                List<BigInteger> contractBalances1 =
                        IntStream.range(0, userCount)
                                .parallel()
                                .mapToObj(
                                        i -> {
                                            try {
                                                return balanceService.getBalance(
                                                        eoaCryptoSuites[i]
                                                                .getCryptoKeyPair()
                                                                .getAddress());
                                            } catch (ContractException e) {
                                                e.printStackTrace();
                                            }
                                            return null;
                                        })
                                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                if (contractBalances1.stream()
                                .reduce(BigInteger::add)
                                .get()
                                .compareTo(BigInteger.valueOf(100000000L * userCount))
                        != 0) {
                    System.out.println(
                            "Verify Balance Failed! TotalBalance: "
                                    + contractBalances1.stream().reduce(BigInteger::add).get()
                                    + ", Expected: "
                                    + BigInteger.valueOf(100000000L * userCount));
                } else {
                    System.out.println(
                            "Verify Balance Success! TotalBalance: "
                                    + contractBalances1.stream().reduce(BigInteger::add).get()
                                    + ", Expected: "
                                    + BigInteger.valueOf(100000000L * userCount));
                }
                break;
            default:
                System.out.println("invalid command: " + command);
                break;
        }
    }
}
