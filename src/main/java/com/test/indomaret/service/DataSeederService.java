package com.test.indomaret.service;

import com.test.indomaret.entity.Role;
import com.test.indomaret.entity.User;
import com.test.indomaret.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSeederService implements CommandLineRunner {

    private final ProvinceRepository provinceRepository;
    private final BranchRepository branchRepository;
    private final StoreRepository storeRepository;
    private final WhitelistStoreRepository whitelistStoreRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    private static final String[][] PROVINCES = {
            {"Aceh", "ACE"}, {"Sumatera Utara", "SUMUT"}, {"Sumatera Barat", "SUMBAR"},
            {"Riau", "RIAU"}, {"Kepulauan Riau", "KEPRI"}, {"Jambi", "JAMBI"},
            {"Sumatera Selatan", "SUMSEL"}, {"Kepulauan Bangka Belitung", "BABEL"},
            {"Bengkulu", "BENGKULU"}, {"Lampung", "LAMPUNG"}, {"DKI Jakarta", "JKT"},
            {"Banten", "BANTEN"}, {"Jawa Barat", "JABAR"}, {"Jawa Tengah", "JATENG"},
            {"DI Yogyakarta", "DIY"}, {"Jawa Timur", "JATIM"}, {"Bali", "BALI"},
            {"Nusa Tenggara Barat", "NTB"}, {"Nusa Tenggara Timur", "NTT"},
            {"Kalimantan Barat", "KALBAR"}, {"Kalimantan Tengah", "KALTENG"},
            {"Kalimantan Selatan", "KALSEL"}, {"Kalimantan Timur", "KALTIM"},
            {"Kalimantan Utara", "KALTARA"}, {"Sulawesi Utara", "SULUT"},
            {"Gorontalo", "GORONTALO"}, {"Sulawesi Tengah", "SULTENG"},
            {"Sulawesi Barat", "SULBAR"}, {"Sulawesi Selatan", "SULSEL"},
            {"Sulawesi Tenggara", "SULTRA"}, {"Maluku", "MALUKU"},
            {"Maluku Utara", "MALUT"}, {"Papua", "PAPUA"}, {"Papua Barat", "PAPBAR"},
            {"Papua Selatan", "PAPSEL"}, {"Papua Tengah", "PAPTEN"},
            {"Papua Pegunungan", "PAPPEG"}, {"Papua Barat Daya", "PAPBD"}
    };

    @Override
    public void run(String... args) {
        seedUsers();
        seedProvinces();
        seedBranches();
        seedInitialStoresAndWhitelist();
    }

    @Transactional
    public void seedUsers() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@indomaret.co.id")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("System Administrator")
                    .role(Role.ROLE_ADMIN)
                    .build();
            admin.setIsActive(true);
            admin.setIsDeleted(false);
            userRepository.save(admin);
            log.info("Default admin user created: admin / admin123");
        }

        if (!userRepository.existsByUsername("user")) {
            User normalUser = User.builder()
                    .username("user")
                    .email("user@indomaret.co.id")
                    .password(passwordEncoder.encode("user123"))
                    .fullName("Regular User")
                    .role(Role.ROLE_USER)
                    .build();
            normalUser.setIsActive(true);
            normalUser.setIsDeleted(false);
            userRepository.save(normalUser);
            log.info("Default user created: user / user123");
        }
    }

    @Transactional
    public void seedProvinces() {
        if (provinceRepository.count() == 0) {
            log.info("Seeding 38 Indonesian provinces...");
            for (String[] prov : PROVINCES) {
                com.test.indomaret.entity.Province p = com.test.indomaret.entity.Province.builder()
                        .name(prov[0])
                        .code(prov[1])
                        .build();
                p.setIsActive(true);
                p.setIsDeleted(false);
                provinceRepository.save(p);
            }
            log.info("Provinces seeded successfully.");
        }
    }

    @Transactional
    public void seedBranches() {
        if (branchRepository.count() == 0) {
            log.info("Seeding 100 branches across provinces...");
            List<com.test.indomaret.entity.Province> provinces = provinceRepository.findAll();
            if (provinces.isEmpty()) return;

            int branchIndex = 1;
            for (int i = 0; i < 100; i++) {
                com.test.indomaret.entity.Province province = provinces.get(i % provinces.size());
                String code = String.format("BR-%s-%03d", province.getCode(), branchIndex);
                String name = "Cabang Indomaret " + province.getName() + " " + ((i / provinces.size()) + 1);

                com.test.indomaret.entity.Branch branch = com.test.indomaret.entity.Branch.builder()
                        .name(name)
                        .code(code)
                        .address("Jl. Raya Utama No. " + (i + 1) + ", " + province.getName())
                        .province(province)
                        .build();
                branch.setIsActive(true);
                branch.setIsDeleted(false);
                branchRepository.save(branch);
                branchIndex++;
            }
            log.info("100 branches seeded successfully.");
        }
    }

    @Transactional
    public void seedInitialStoresAndWhitelist() {
        if (storeRepository.count() == 0) {
            log.info("Seeding initial stores and whitelist stores...");
            List<com.test.indomaret.entity.Branch> branches = branchRepository.findAll();
            if (branches.isEmpty()) return;

            // Seed initial 500 stores
            seedBulkStores(500);

            // Add first 5 stores to whitelist
            List<com.test.indomaret.entity.Store> firstStores = storeRepository.findAll().stream().limit(5).toList();
            for (int i = 0; i < firstStores.size(); i++) {
                com.test.indomaret.entity.Store s = firstStores.get(i);
                com.test.indomaret.entity.WhitelistStore ws = com.test.indomaret.entity.WhitelistStore.builder()
                        .store(s)
                        .reason("Flagship Store Hub #" + (i + 1))
                        .build();
                ws.setIsActive(true);
                ws.setIsDeleted(false);
                whitelistStoreRepository.save(ws);
            }
            log.info("Initial stores and 5 whitelist stores seeded.");
        }
    }

    /**
     * Ultra-fast bulk store generator using JDBC batch updates.
     * Can generate 20,000+ stores in ~2 seconds.
     */
    public int seedBulkStores(int targetCount) {
        long currentCount = storeRepository.count();
        if (currentCount >= targetCount) {
            log.info("Store count already {} >= target {}", currentCount, targetCount);
            return (int) currentCount;
        }

        int toGenerate = (int) (targetCount - currentCount);
        log.info("Bulk seeding {} stores using JDBC batching...", toGenerate);

        List<Long> branchIds = jdbcTemplate.queryForList(
                "SELECT id FROM branches WHERE is_active = true AND is_deleted = false",
                Long.class
        );

        if (branchIds.isEmpty()) {
            throw new IllegalStateException("No active branches found to attach stores to");
        }

        String sql = "INSERT INTO stores (name, code, address, branch_id, is_active, is_deleted, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        int batchSize = 1000;
        List<Object[]> batchArgs = new ArrayList<>(batchSize);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        long startIdx = currentCount + 1;
        int numBranches = branchIds.size();

        for (int i = 0; i < toGenerate; i++) {
            long storeNum = startIdx + i;
            Long branchId = branchIds.get((int) (i % numBranches));
            String code = String.format("STR-%06d", storeNum);
            String name = "Indomaret Store " + storeNum;
            String address = "Jl. Sudirman / Gatot Subroto No. " + (i % 500 + 1);

            batchArgs.add(new Object[]{
                    name,
                    code,
                    address,
                    branchId,
                    true,
                    false,
                    now,
                    now
            });

            if (batchArgs.size() == batchSize) {
                jdbcTemplate.batchUpdate(sql, batchArgs);
                batchArgs.clear();
            }
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
            batchArgs.clear();
        }

        long finalCount = storeRepository.count();
        log.info("Bulk seed completed. Total stores in DB: {}", finalCount);
        return (int) finalCount;
    }
}
