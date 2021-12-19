package database.rzotgorz.utils;

import database.rzotgorz.recordsystem.FileHandler;
import database.rzotgorz.recordsystem.RID;
import database.rzotgorz.recordsystem.Record;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

@Slf4j
public class FileScanner implements Iterable<Record> {

    private final FileHandler fileHandler;

    public class RecordIterator implements Iterator<Record> {
        private int count;
        private int slotId;

        private void moveToNextValidRecord() {
            OUT:
            for (; count < fileHandler.getFileHeader().getInteger("pageNum"); count++) {
                byte[] page = fileHandler.getPage(count);
                if (page[0] != 0)
                    continue;
                byte[] bitmap = fileHandler.getBitmap(page);
                slotId++;
//                System.out.println("-------");
//                for (int i = 0; i < bitmap.length; i++) {
//                    System.out.println(bitmap[i]);
//                }

                for (; slotId < bitmap.length; slotId++)
                    if ((bitmap[slotId / 8] & (1 << (slotId & 7))) > 0)
                        break OUT;
                slotId = -1;
            }
        }

        private Record getCurrentRecord() {
            return fileHandler.getRecord(new RID(count, slotId));
        }

        public RecordIterator() {
            this.count = 1;
            this.slotId = -1;
            moveToNextValidRecord();
        }

        @Override
        public boolean hasNext() {
            return count < fileHandler.getFileHeader().getInteger("pageNum") && slotId < fileHandler.getBitmap(fileHandler.getPage(count)).length;
        }

        @Override
        public Record next() {
            Record record = getCurrentRecord();
            moveToNextValidRecord();
            return record;
        }

        @Override
        public void remove() {
            Iterator.super.remove();
        }

        @Override
        public void forEachRemaining(Consumer<? super Record> action) {
            Iterator.super.forEachRemaining(action);
        }
    }

    public FileScanner(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }


    @Override
    public Iterator<Record> iterator() {
        return new RecordIterator();
    }

    @Override
    public void forEach(Consumer<? super Record> action) {
        Iterable.super.forEach(action);
    }

    @Override
    public Spliterator<Record> spliterator() {
        return Iterable.super.spliterator();
    }
}
