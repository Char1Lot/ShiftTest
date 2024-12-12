package ru.char1lot;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

/*
 * Основной класс реализующий бизнес-логику утилиты
 * * Собирает и обрабатывает статистику из входящих файлов
 * * Фильтрует данные из входящих файлов на 3 категории(строки, целые числа, дробные числа)
 * * Сохраняет отфильтрованные данные в 3 файла соответственно
 * * Статистика не сбрасывается с каждым запуском программы, в рабочей директории утилиты файл stat.txt
 *  хранит в себе информацию о прошлых запусках программы и тогдашних результатах статистики, это было реализовано в связи
 *  с необходимостью иметь возможность дозаписи в выходные файлы, а в ТЗ было указано сбор статистики в процессе фильтрации
 *  конечно, я бы мог сделать так, что бы статистика выводилась из уже сформированных файлов, однако такой подход
 *  не соответствовал техническому заданию, и было принято решение "изобретать колесо", однако мне понравилось, на таких мини-проектах
 *  и получается лучше всего учиться, спасибо.
 * */

public class FileManagement {

    private int maxLenStr, minLenStr;

    private int lenIntFile, lenStrFile, lenFloatFile;

    private int maxInt, minInt, middleInt, sumInt;

    private float maxFloat, minFloat, middleFloat, sumFloat;

    private short typeIdentify(String str){
        if(str.matches("-?\\d+")){
            return 0;
        } else if (str.matches("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?")) {
            return  1;
        }
        return 2;
    }

    /*
     * Метод, который отвечает за хранение ранее собранной статистики, слияние старой статистики с новой, создает и обновляет файл со статистикой
     * это необходимо, потому что в ТЗ было прописано возможность дозаписи данных в существующие файлы, таким образом сбор статистики
     * по ходу фильтрации новых данных становится невозможным, ведь в ТЗ четко было прописано что статистика должна собираться по ходу фильтрации
     * и таким образом я решил эту проблему, создав отдельный файл, хранящий данные о прошлых сборах статистики
     * в случае если пользователь не указал флаг "-а" файл с прошлой статистикой очищается, так как файлы с отфильтрованными данными будут перезаписаны
     *
     * Метод работает таким образом, что из файла со статистикой построчно считывается каждая часть статистики в строгом порядке, как объявлены поля этого класса
     *
     * Метод принимает 2 аргумента:
     *                  statFile - путь до директории, где будет аккумулироваться статистика прошлых фильтраций
     *                  isNeedReWrite - флаг, который определяет, нужно ли стирать прошлую статистику, поскольку если пользователь
     *                                  не укажет флаг "-а" будет произведена перезапись файлов с данными, и, соответственно, старая статистика станет неактуальной
     * */

    private void updateStatFile(String statFile, boolean isNeedReWrite) throws IOException {

        statFile += "/stat.txt";

        Path pathToStatFile = Path.of(statFile);

        /*
         * Проверяем нужно ли делать перезапись файлов, и в случае есть да, удаляем старый файл со статистикой
         * */

        if(isNeedReWrite){
            Files.deleteIfExists(pathToStatFile);
        }

        /*
         * Проверяем, существует ли файл со статистикой, и если нет то создаем пустой txt файл
         * в который записываем данные в строгом порядке, как они объявлены в классе.
         * Это будет являться первой итерацией записи статистики,при дальнейших запусках программы эти данные будут обновляться
         * и таким образом через N запусков не теряется статистика за предыдущие запуски.
         * */

        if(!Files.exists(pathToStatFile)){

            Files.createFile(pathToStatFile);

            writeStatToFile(pathToStatFile);

            return;

        }

        /*
         * Я понимаю что возможно это не самый лучший подход. Что можно создать два массива, для целочисленных данных и для дробных
         * и сделать тут цикл в котором будет проверяться только тип данных которые считываются, но я считаю что мой код
         * более читаем чем код в котором будет два массива непонятно какого наполнения
         * */

        /*
         * Здесь происходит считывание данных в строгом порядке из файла со статистикой, эти данные сравниваются с теми что получились
         * в результате очередной фильтрации входных файлов, таким образом поля класса остаются с актуальными значениями
         * например если N запусков утилиты назад было найдено наименьшее целое число, то это число останется в памяти, а не уничтожится
         * по завершении работы программы.
         * */
        try(BufferedReader reader = Files.newBufferedReader(pathToStatFile)){

            String line;
            int index = 0;

            while ((line = reader.readLine()) != null){

                updateField(index, line);
                index++;

            }

            /*
             * Обновив поля класса до актуальных значений, файл со статистикой перезаписывается, и актуальные значения сохранятся
             * в долговременную память, таким образом я решил проблему сохранения статистики по ходу фильтрации, а не после нее.
             * ещё раз, учитывая то что я прочитал в ТЗ, я не мог просто прочитать уже сформированные файлы и собрать статистику из них
             * мне было необходимо реализовать функционал, который предусматривают сбор статистики по ходу фильтрации, но не иначе.
             * */

            writeStatToFile(pathToStatFile);


        } catch (IOException e) {

            e.printStackTrace();

        }


    }

    private void updateField(int index, String value){
        switch (index) {
            case 0:
                maxLenStr = Math.max(maxLenStr, Integer.parseInt(value));
                break;
            case 1:
                minLenStr = Math.min(minLenStr, Integer.parseInt(value));
                break;
            case 2:
                lenIntFile += Integer.parseInt(value);
                break;
            case 3:
                lenStrFile += Integer.parseInt(value);
                break;
            case 4:
                lenFloatFile += Integer.parseInt(value);
                break;
            case 5:
                maxInt = Math.max(maxInt, Integer.parseInt(value));
                break;
            case 6:
                minInt = Math.min(minInt, Integer.parseInt(value));
                break;
            case 7:
                sumInt += Integer.parseInt(value);
                break;
            case 8:
                maxFloat = Math.max(maxFloat, Float.parseFloat(value));
                break;
            case 9:
                minFloat = Math.min(minFloat, Float.parseFloat(value));
                break;
            case 10:
                sumFloat += Float.parseFloat(value);
                break;
            default:
                throw new IllegalArgumentException("Unexpected index: " + index);
        }
    }

    private void writeStatToFile(Path pathToStatFile) throws IOException {

        String statToWrite = "" + maxLenStr + '\n' +
                minLenStr + '\n' +
                lenIntFile + '\n' +
                lenStrFile + '\n' +
                lenFloatFile + '\n' +
                maxInt + '\n' +
                minInt + '\n' +
                sumInt + '\n' +
                maxFloat + '\n' +
                minFloat + '\n' +
                sumFloat;

        Files.writeString(pathToStatFile, statToWrite);
    }

    /*
     * Конструктор без аргументов, тут все понятно
     * */

    public FileManagement(){

        maxLenStr = Integer.MIN_VALUE;
        minLenStr = Integer.MAX_VALUE;

        lenIntFile = 0;
        lenFloatFile = 0;
        lenStrFile = 0;

        maxInt = Integer.MIN_VALUE;
        minInt = Integer.MAX_VALUE;
        middleInt = 0;
        sumInt = 0;

        maxFloat = Float.MIN_VALUE;
        minFloat = Float.MAX_VALUE;
        middleFloat = 0;
        sumFloat = 0;

    }

    /*
     * Основной метод класса, который поочередно и построчно считывает данные из входных файлов, и, основываясь
     * на типе данных, который он считал, добавляет запись с новой строки в один из трех файлов - строки, целые или дробные числа.
     * Метод принимает 4 аргумента :
     *                  outPath - путь до выходной директории, куда будут сохраняться файлы с данными
     *                  prefix - префикс для названий файлов, например префикс out_ будет обозначать что будут создаваться файлы с названием out_strings.txt и т.д
     *                  inPaths - массив путей до файлов, который необходимо считать
     *                  isNeedRewrite - флаг, обозначающий необходимость дозаписи в уже существующие файлы, или перезапись этих файлов.
     * */

    public void fileFilter(String outPath, String prefix, List<String> inPaths, boolean isNeedRewrite) throws IOException {

        List<Path> outPathList = new LinkedList<>();

        outPathList.add(Path.of(outPath + "/" + prefix + "integers.txt"));
        outPathList.add(Path.of(outPath + "/" + prefix + "floats.txt"));
        outPathList.add(Path.of(outPath + "/" + prefix + "strings.txt"));

        /*
         * В случае не укзания флага "-а" выходные файлы, получившиеся в результате прошлых запусков этой утилиты, перезаписываются.
         * */

        if(isNeedRewrite){

            for(Path itPath : outPathList){

                Files.deleteIfExists(itPath);

            }

        }

        /*
         * В цикле считываются входные файлы в том порядке, в котором они расположены во входном массиве, вернее будет сказать что считываются не файлы из массива
         * а путь к этим файлам.
         * */

        for(String itStr : inPaths){

            Path inputFilePath = Path.of(itStr);

            /*
             * Построчно читаются входные файлы, считанная строка проверяется регулярным выражением сначала на принадлежность к целым числам
             * затем к числам с дробной частью
             * затем, если считанная строка не является числом, она будет интерпретирована как строка
             * */

            try (BufferedReader reader = Files.newBufferedReader(inputFilePath)) {

                String line;

                while ((line = reader.readLine()) != null) {

                    short type = typeIdentify(line);

                    if(!Files.exists(outPathList.get(type))){
                           Files.createFile(outPathList.get(type));
                    }

                    Files.write(outPathList.get(type), (line + '\n').getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

                    switch (type){

                        case 0:
                            maxInt = Math.max(maxInt, Integer.parseInt(line));
                            minInt = Math.min(minInt, Integer.parseInt(line));
                            sumInt += Integer.parseInt(line);
                            lenIntFile++;
                            break;

                        case 1:
                            maxFloat = Math.max(maxFloat, Float.parseFloat(line));
                            minFloat = Math.min(minFloat, Float.parseFloat(line));
                            sumFloat += Float.parseFloat(line);
                            lenFloatFile++;
                            break;

                        case 2:
                            maxLenStr = Math.max(maxLenStr, line.length());
                            minLenStr = Math.min(minLenStr,  line.length());
                            lenStrFile++;
                            break;

                        default:
                            throw new IOException();

                    }
                }
            } catch (IOException e) {

                e.printStackTrace();

            }
        }

        middleFloat = (maxFloat + minFloat) / 2;
        middleInt = (maxInt + middleInt) / 2;

        /*
         * После сбора статистики из всех входных файлов эти данные аккумулируются с данными из файла со статистикой.
         * */

        updateStatFile(Paths.get("").toAbsolutePath().toString(), isNeedRewrite);

    }

    public String fileFullAnalytics() throws IOException {


        String res = "full statistic : "  + '\n' +
                "---------------------------------------" + '\n'+
                "length str file : " + lenStrFile + '\n' +
                "shortest str length : " + minLenStr + '\n' +
                "longest str length : " + maxLenStr + '\n' +
                "---------------------------------------" + '\n' +
                "length int file : " + lenIntFile + '\n' +
                "shortest int : " + minInt + '\n' +
                "longest int : " + maxInt + '\n' +
                "sum int : " + sumInt + '\n' +
                "middle int : " + middleInt  + '\n' +
                "---------------------------------------" + '\n' +
                "length float file : " + lenFloatFile + '\n' +
                "shortest float length : " + minFloat + '\n' +
                "longest float length : " + maxFloat + '\n' +
                "sum float : " + sumFloat + '\n' +
                "middle float : " + middleFloat + '\n';

        return res;
    }

    public String fileShortAnalytics() throws IOException {

        String res = "short statistic : " + '\n' +
                "----------------------------" + '\n' +
                "Strings file length  : " + lenStrFile + '\n' +
                "Integers file length : " + lenIntFile + '\n' +
                "Floats file length : " + lenFloatFile + '\n';

        return res;
    }
}