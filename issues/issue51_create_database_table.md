Create database table for Animals


# Acceptance Criteria:

- [ ] There is an `@Entity` class called Animal.java
- [ ] There is a `@Repository` class called AnimalRepository.java
- [ ] When you start up the repo on localhost, you can see the table
      using the H2 console (see the file `docs/h2-database.md` for 
      instructions.)
- [ ] You can see the animals table when you do these steps:
      1. Connect to postgres command line with 
         ```
         dokku postgres:connect team02-qa-db
         ```
      2. Enter `\dt` at the prompt. You should see
         `animals` listed in the table.
      3. Use `\q` to quit


