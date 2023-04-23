package academy.prog;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// DB -> E(20) -> R -> S -> DTO <- C -> View / JSON (5)

@Service
public class UrlService {
    private final UrlRepository urlRepository;

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Transactional
    public long saveUrl(UrlDTO urlDTO) {
        var urlRecord = urlRepository.findByUrl(urlDTO.getUrl());
        if (urlRecord == null) {
            urlRecord = UrlRecord.of(urlDTO);
            urlRepository.save(urlRecord);
        }

        return urlRecord.getId();
    }

    @Transactional
    public String getUrl(long id) {
        var urlOpt = urlRepository.findById(id);
        if (urlOpt.isEmpty())
            return null;

        var urlRecord = urlOpt.get();
        urlRecord.setCount(urlRecord.getCount() + 1);
        urlRecord.setLastAccess(new Date());

        return urlRecord.getUrl();
    }

    @Transactional(readOnly = true)
    public List<UrlStatDTO> getStatistics() {
        var records = urlRepository.findAll();
        var result = new ArrayList<UrlStatDTO>();

        records.forEach(x -> result.add(x.toStatDTO()));

        return result;
    }

    //delete URL
    @Transactional
    public boolean deleteUrl(long id) {
        var urlOpt = urlRepository.findById(id);
        if (urlOpt.isEmpty()) {
            return false;
        }
        var urlRecord = urlOpt.get();
        urlRecord.getCount(); // the number of uses of the record
        urlRepository.delete(urlRecord);
        System.out.println("ok");
        return true;
    }

    // delete old URL (age 30 days)
    @Transactional
    @Scheduled(cron = "${0 0 * * 1}") //every Monday at 0:00
    public void deleteOld() {
        var urlOpt = urlRepository.findAll();
        for (UrlRecord record : urlOpt) {
            Date lastAccess = record.getLastAccess();
            Date today = new Date();
            Duration duration = Duration.ofDays(today.getTime() - lastAccess.getTime());
            if (duration.toDays() > 30) {
                urlRepository.delete(record);
            }
        }
    }

}
