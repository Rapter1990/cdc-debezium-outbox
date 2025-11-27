package com.example.notificationservice.service;

import com.example.notificationservice.base.AbstractBaseServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

class MailServiceTest extends AbstractBaseServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        // Inject @Value fields
        ReflectionTestUtils.setField(mailService, "to", "target@example.com");
        ReflectionTestUtils.setField(mailService, "from", "no-reply@example.com");
    }

    @Test
    void send_shouldBuildSimpleMailMessageAndDelegateToJavaMailSender() {

        // Given
        String subject = "Test Subject";
        String text = "Test Body";

        final SimpleMailMessage[] sentHolder = new SimpleMailMessage[1];

        // When
        doAnswer(invocation -> {
            sentHolder[0] = invocation.getArgument(0);
            return null;
        }).when(mailSender).send(any(SimpleMailMessage.class));


        // Then
        mailService.send(subject, text);

        SimpleMailMessage sent = sentHolder[0];
        assertArrayEquals(new String[]{"target@example.com"}, sent.getTo());
        assertEquals("no-reply@example.com", sent.getFrom());
        assertEquals(subject, sent.getSubject());
        assertEquals(text, sent.getText());

        // Verify
        verify(mailSender).send(any(SimpleMailMessage.class));

    }

}