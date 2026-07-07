package com.example.ecommerce_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;

@SpringBootApplication
public class EcommerceBackendApplication implements CommandLineRunner {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		SpringApplication.run(EcommerceBackendApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("--- Kiem tra va xoa check constraint tren product_status ---");
		try {
			String findConstraintSql = 
				"SELECT cc.name FROM sys.check_constraints cc " +
				"JOIN sys.columns c ON cc.parent_object_id = c.object_id AND cc.parent_column_id = c.column_id " +
				"WHERE cc.parent_object_id = OBJECT_ID('products') AND c.name = 'product_status'";
			
			List<String> constraintNames = jdbcTemplate.queryForList(findConstraintSql, String.class);
			for (String constraintName : constraintNames) {
				System.out.println("Tim thay check constraint: " + constraintName);
				jdbcTemplate.execute("ALTER TABLE products DROP CONSTRAINT " + constraintName);
				System.out.println("Da xoa check constraint: " + constraintName);
			}
		} catch (Exception e) {
			System.err.println("Loi khi tim/xoa check constraint: " + e.getMessage());
		}

		System.out.println("--- Chay lenh di chuyen database sua loi phong chu ---");
		String[] alterQueries = {
			"ALTER TABLE orders ALTER COLUMN payment_method NVARCHAR(255)",
			"ALTER TABLE orders ALTER COLUMN shipping_address NVARCHAR(255)",
			"ALTER TABLE payments ALTER COLUMN payment_method NVARCHAR(255)",
			"ALTER TABLE user_addresses ALTER COLUMN full_name NVARCHAR(100)",
			"ALTER TABLE user_addresses ALTER COLUMN address_detail NVARCHAR(255)"
		};

		for (String sql : alterQueries) {
			try {
				jdbcTemplate.execute(sql);
				System.out.println("Thuc thi thanh cong: " + sql);
			} catch (Exception e) {
				System.err.println("Bo qua hoac loi thuc thi (" + sql + "): " + e.getMessage());
			}
		}
	}

}
