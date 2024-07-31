package service;

import static exception.ErrorMessage.*;
import static formatter.MailMessageFormatter.*;
import static java.lang.String.*;

import java.sql.SQLException;
import javax.mail.MessagingException;

import domain.Member;
import domain.Stamp;
import dto.response.MyStampDto;
import repository.MemberRepository;
import repository.StampRepository;
import util.MailAPIConfigLoader;

public class StampService {
	private final StampRepository stampRepository;
	private final MemberRepository memberRepository;
	private final MailService mailService;
	private static final String EMAIL = MailAPIConfigLoader.getEmail();

	public StampService(StampRepository stampRepository, MemberRepository memberRepository, MailService mailService) {
		this.stampRepository = stampRepository;
		this.memberRepository = memberRepository;
		this.mailService = mailService;
	}

	public MyStampDto findMyStamp(long memberId, long cafeId) {
		Stamp stamp = stampRepository.findStamp(memberId, cafeId);
		return MyStampDto.createMyStampDto(stamp);
	}

	public void saveStamp(long cafeId, String userPhoneNum, int count) throws SQLException {	//스탬프 저장하는 메서드
		Member findUser = memberRepository.findUserByPhoneNum(userPhoneNum);
		if(findUser == null){
			throw new IllegalArgumentException(NOT_FOUND_MEMBER.getErrorMessage());
		}
		stampRepository.save(findUser.getId(), cafeId, count);
	}

	//친구에게 스탬프 공유하는 메서드
	public void shareStamp(Member fromMember, long cafeId, String toPhoneNum, int count) throws MessagingException, SQLException {
		Member toMember = memberRepository.findUserByPhoneNum(toPhoneNum);
		if(toMember == null){
			throw new IllegalArgumentException(NOT_FOUND_MEMBER.getErrorMessage());
		}
		if (stampRepository.updateStamp(cafeId, fromMember.getId(), toMember.getId(), count)) {	//성공한 경우
			mailService.sendMail(toMember.getEmail(),format(SEND_STAMP_MESSAGE.getMessage(), count, toMember.getUserName()), SHARE_STAMP_TITLE.getMessage());
			mailService.sendMail(toMember.getEmail(), format(RECEIVE_STAMP_MESSAGE.getMessage(), fromMember.getUserName(), count), SHARE_STAMP_TITLE.getMessage());
		} else {	//실패 한 경우
			mailService.sendMail(toMember.getEmail(), FAIL_SEND_STAMP_MESSAGE.getMessage(), SHARE_STAMP_TITLE.getMessage());
			throw new IllegalArgumentException("스탬프 공유 실패");
		}
	}
}
