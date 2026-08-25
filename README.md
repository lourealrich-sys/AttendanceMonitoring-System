# Attendance Monitoring System

A Java Swing-based **Attendance Monitoring System** for recording and monitoring student attendance across a five-day school week. The system provides a graphical interface for managing student records, marking students as Present or Absent, viewing attendance statistics, searching records, and saving attendance data to a text file (File Handling).

## Features

- Add student records using an ID number and name
- Edit existing student information
- Delete student records
- Prevent duplicate student IDs
- Search student records
- Select attendance dates from the generated five-day schedule
- Mark students as:
  - Present
  - Absent
- Automatically calculate:
  - Total Present
  - Total Absent
  - Total Attendance
  - Attendance Percentage
- Save attendance records to a text file
- Load previously saved attendance records when the application starts
- Restrict date selection to the generated attendance dates
- Java Swing graphical user interface
- Uses a table-based attendance dashboard

## Technologies Used

- **Java**
- **Java Swing** – graphical user interface
- **JTable / DefaultTableModel** – attendance table
- **File I/O** – saving and loading attendance records
- **JDateChooser** – date selection
- **Java AWT** – interface layout and styling

## System Structure

The system is organized around the following main components:

### Student Management

Students can be added using:

- ID Number
- Name

The system checks for duplicate IDs before adding or updating a record.

### Attendance Management

The user selects one of the generated attendance dates and uses the **PRESENT** or **ABSENT** buttons for each student.

Attendance values are stored internally as:

- `1` = Present
- `2` = Absent
- `0` = No attendance recorded

### Attendance Statistics

For every student, the system calculates:

```text
Total Present
Total Absent
Total Attendance
Attendance Percentage
