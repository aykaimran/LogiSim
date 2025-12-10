// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package org.yourcompany.yourproject.dataAccessLayer.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
   private static final String URL = "<write url here>";
   private static final String USER = "<write user here>";
   private static final String PASSWORD = "<write password here>";

   public DatabaseUtil() {
   }

   public static Connection getConnection() {
      System.out.println("\ud83d\udd17 Attempting Neon PostgreSQL connection...");
      System.out.println("  URL: " + "jdbc:postgresql://ep-summer-band-adzvqa20-pooler.c-2.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require".replaceAll("npg_.+", "npg_*"));

      try {
         Connection var0 = DriverManager.getConnection("jdbc:postgresql://ep-summer-band-adzvqa20-pooler.c-2.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require", "neondb_owner", "npg_p09PtIkhXlQV");
         System.out.println("✓ Neon PostgreSQL connection SUCCESSFUL");
         return var0;
      } catch (SQLException var1) {
         System.out.println("✗ NEON POSTGRESQL CONNECTION FAILED!");
         System.out.println("  Error: " + var1.getMessage());
         System.out.println("  SQL State: " + var1.getSQLState());
         if (var1.getMessage().contains("authentication failed")) {
            System.out.println("\ud83d\udca1 SOLUTION: Check your Neon.tech credentials");
         } else if (var1.getMessage().contains("Connection refused")) {
            System.out.println("\ud83d\udca1 SOLUTION: Check your internet connection and Neon.tech status");
         }

         return null;
      }
   }

   public static boolean testConnection() {
      System.out.println("\n\ud83e\uddea Testing Neon PostgreSQL Connection...");

      try {
         Connection var0 = getConnection();

         boolean var1;
         label52: {
            try {
               if (var0 == null || var0.isClosed()) {
                  System.out.println("✗ Neon PostgreSQL connection test: FAILED");
                  var1 = false;
                  break label52;
               }

               System.out.println("✓ Neon PostgreSQL connection test: SUCCESS");
               var1 = true;
            } catch (Throwable var4) {
               if (var0 != null) {
                  try {
                     var0.close();
                  } catch (Throwable var3) {
                     var4.addSuppressed(var3);
                  }
               }

               throw var4;
            }

            if (var0 != null) {
               var0.close();
            }

            return var1;
         }

         if (var0 != null) {
            var0.close();
         }

         return var1;
      } catch (SQLException var5) {
         System.out.println("✗ Neon PostgreSQL connection test: FAILED with exception");
         var5.printStackTrace();
         return false;
      }
   }

   static {
      try {
         Class.forName("org.postgresql.Driver");
         System.out.println("✓ PostgreSQL JDBC Driver loaded successfully");
      } catch (ClassNotFoundException var1) {
         System.out.println("✗ ERROR: PostgreSQL JDBC Driver not found!");
         System.out.println("  Add this to your pom.xml:");
         System.out.println("  <dependency>");
         System.out.println("    <groupId>org.postgresql</groupId>");
         System.out.println("    <artifactId>postgresql</artifactId>");
         System.out.println("    <version>42.6.0</version>");
         System.out.println("  </dependency>");
         var1.printStackTrace();
      }

   }
}