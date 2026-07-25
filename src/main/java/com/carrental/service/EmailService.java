package com.carrental.service;

import com.carrental.entity.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:default@email.com}")
    private String fromEmail;

    @Async
    public void sendBookingConfirmationEmail(Booking booking) {
        try {
            if (mailSender == null) return;
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("Booking Confirmed - Car Rental #" + booking.getId());
            helper.setText(buildBookingEmailHtml(booking), true);
            mailSender.send(message);
            System.out.println("Confirmation email sent to: " + booking.getUser().getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    @Async
    public void sendCancellationEmail(Booking booking) {
        try {
            if (mailSender == null) return;
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("Booking Cancelled - Car Rental #" + booking.getId());
            helper.setText(buildCancellationEmailHtml(booking), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send cancellation email: " + e.getMessage());
        }
    }

    private String buildBookingEmailHtml(Booking booking) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                <div style="background: #2c3e50; color: white; padding: 20px; text-align: center;">
                    <h1>Car Rental System</h1>
                    <h2>Booking Confirmed!</h2>
                </div>
                <div style="padding: 20px; background: #f9f9f9;">
                    <p>Dear <b>%s</b>,</p>
                    <p>Your booking has been confirmed. Here are your booking details:</p>
                    <table style="width:100%%; border-collapse: collapse;">
                        <tr style="background:#e8e8e8;">
                            <td style="padding:10px; border:1px solid #ddd;"><b>Booking ID</b></td>
                            <td style="padding:10px; border:1px solid #ddd;">#%d</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; border:1px solid #ddd;"><b>Car</b></td>
                            <td style="padding:10px; border:1px solid #ddd;">%s %s</td>
                        </tr>
                        <tr style="background:#e8e8e8;">
                            <td style="padding:10px; border:1px solid #ddd;"><b>License Plate</b></td>
                            <td style="padding:10px; border:1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; border:1px solid #ddd;"><b>From</b></td>
                            <td style="padding:10px; border:1px solid #ddd;">%s</td>
                        </tr>
                        <tr style="background:#e8e8e8;">
                            <td style="padding:10px; border:1px solid #ddd;"><b>To</b></td>
                            <td style="padding:10px; border:1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; border:1px solid #ddd;"><b>Total Days</b></td>
                            <td style="padding:10px; border:1px solid #ddd;">%d days</td>
                        </tr>
                        <tr style="background:#e8e8e8;">
                            <td style="padding:10px; border:1px solid #ddd;"><b>Base Amount</b></td>
                            <td style="padding:10px; border:1px solid #ddd;">Rs.%.2f</td>
                        </tr>
                        <tr>
                            <td style="padding:10px; border:1px solid #ddd;"><b>GST (18%%)</b></td>
                            <td style="padding:10px; border:1px solid #ddd;">Rs.%.2f</td>
                        </tr>
                        <tr style="background:#27ae60; color:white;">
                            <td style="padding:10px; border:1px solid #ddd;"><b>Total Paid</b></td>
                            <td style="padding:10px; border:1px solid #ddd;"><b>Rs.%.2f</b></td>
                        </tr>
                    </table>
                    <p style="margin-top:20px;">Payment ID: <b>%s</b></p>
                    <p>Thank you for choosing Car Rental System!</p>
                </div>
                <div style="background:#2c3e50; color:white; padding:10px; text-align:center;">
                    <p>Car Rental System | Support: support@carrental.com</p>
                </div>
            </body>
            </html>
        """.formatted(
                booking.getUser().getName(),
                booking.getId(),
                booking.getCar().getBrand(), booking.getCar().getModel(),
                booking.getCar().getLicensePlate(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getTotalDays(),
                booking.getTotalAmount(),
                booking.getTaxAmount(),
                booking.getFinalAmount(),
                booking.getRazorpayPaymentId()
        );
    }

    private String buildCancellationEmailHtml(Booking booking) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                <div style="background: #e74c3c; color: white; padding: 20px; text-align: center;">
                    <h1>Car Rental System</h1>
                    <h2>Booking Cancelled</h2>
                </div>
                <div style="padding: 20px;">
                    <p>Dear <b>%s</b>,</p>
                    <p>Your booking <b>#%d</b> for <b>%s %s</b> has been cancelled.</p>
                    <p>If you paid, a refund will be processed within 5-7 business days.</p>
                    <p>Thank you for using Car Rental System.</p>
                </div>
            </body>
            </html>
        """.formatted(
                booking.getUser().getName(),
                booking.getId(),
                booking.getCar().getBrand(),
                booking.getCar().getModel()
        );
    }
}