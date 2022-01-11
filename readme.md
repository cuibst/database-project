Database Manage System Project(TeaDB)
------------
This project is written by cyk and llh, for the purpose of the database class 2021~2022 project.

## Usage
1. Dataset has been translated into serveral sqls in folder testcases, run builddatabase.cmd to import those sqls
2. Use `mvn package` to compile
3. The compiled jar is in target, named `dbms-homework-1.0-SNAPSHOT.jar`. Use `java -jar .\target\dbms-homework-1.0-SNAPSHOT.jar` to run the database
4. This jar support command line args. First argument is input file name, second is the output one. See the cmd script for more details.

## Project log

Update in 2021.10.17 22:32
- Project initialized
- antlr and slf4j configured
- See ```database.rzotgorz.Main.java``` for the usage of antlr

Update in 2021.11.01 17:19
- File system implemented
- Need more check

Update in 2021.11.03
- Record System finished
- Basic check done

Update in 2021.12.03
- Index System implemented

Update in 2021.12.08
- Meta System implemented

Update in 2021.12.17
- Finish Basic database control.
- Create, drop, use database are now supported.

Update in 2021.12.18
- Basic insertion is supported

Update in 2021.12.19
- Basic selection is supported

Update in 2021.12.20
- jLine adapted

Update in 2021.12.21
- Update/Delete are now supported

Update in 2021.12.22
- More complex database control implemented
- add/drop index are now supported
- Add basic constraint check

Update in 2022.01.05
- Finish brute force join

Update in 2022.01.06
- Not null constraint is now supported.

Update in 2022.01.07
- finish a more complex join
- fix several bug in index node spliting.

Update in 2022.01.08
- After check hotfixes

