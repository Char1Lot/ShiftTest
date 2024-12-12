package ru.char1lot;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        CmdHandler cmdHandler = new CmdHandler();

        try{

            /*
             * Собираем пользовательский ввод из командной строки
             * */

            CommandLine cmd = cmdHandler.parse(args);
            String prefix = cmdHandler.getPrefix(cmd);
            String outPath = cmdHandler.getOutPath(cmd);
            boolean rewriteNeeded = cmdHandler.isNeedRewrite(cmd);
            List<String> inPaths = cmdHandler.getFileNames(cmd);
            boolean shortStatIsNeeded = cmdHandler.shortStatIsNeeded(cmd);
            boolean fullStatIsNeeded = cmdHandler.fullStatIsNeeded(cmd);

            FileManagement fileManagement = new FileManagement();

            /*
             * Проводим валидацию ввода.
             * */

            if(prefix.matches(".*[\\/\\:\\*\\?\\<\\>\\|\\\\].*")){
                throw new ParseException("You trying to use prefix with any of this '/ \\ : * ?  < > |' symbol, retry without this symbol");
            }

            if(inPaths.isEmpty()){
                throw new ParseException("No one input file found");
            }

            if(outPath.isEmpty()){
                outPath = Paths.get("").toAbsolutePath().toString();
            }

            inPaths.replaceAll(s -> Paths.get("").toAbsolutePath() + "/" + s);

            /*
             * После валидации данных вызываем метод fileFilter и передаем туда все что ввел пользователь
             * */

            fileManagement.fileFilter(outPath,prefix,inPaths,rewriteNeeded);

            /*
             * Проверяем пользовательский ввод на наличие флага "-f" или "-s" обозначающие короткую или полную статистику соответственно
             * */

            if(shortStatIsNeeded || fullStatIsNeeded){
                String stat = "";
                if(fullStatIsNeeded) {
                    stat = fileManagement.fileFullAnalytics();
                    System.out.println(stat);
                }
                if(shortStatIsNeeded){
                    stat = fileManagement.fileShortAnalytics();
                    System.out.println(stat);
                }
            }

        } catch (ParseException e) {

            throw new RuntimeException(e);

        }
    }
}