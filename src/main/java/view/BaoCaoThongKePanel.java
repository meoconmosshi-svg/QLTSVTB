package com.qlts.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.qlts.bus.ThongKeBUS;
import com.qlts.common.Theme;
import com.qlts.component.Dialogs;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;

public class BaoCaoThongKePanel extends JPanel {
    private final ThongKeBUS bus = new ThongKeBUS();
    private final DecimalFormat MONEY_FMT = new DecimalFormat("#,##0");

    private ChartPanel chartTrangThai, chartLoai, chartYeuCau, chartPhieu;
    private JLabel lblTonKho, lblTongTaiSan, lblTongThietBi, lblYeuCauCho;

    public BaoCaoThongKePanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.addTab("🏠 Tổng quan", buildTabTongQuan());
        tabs.addTab("🏷 Tài sản", buildTabTaiSan());
        tabs.addTab("📊 Nghiệp vụ", buildTabNghiepVu());
        add(tabs, BorderLayout.CENTER);
        refreshAll();
    }

    private JPanel buildTabTongQuan() {
        JPanel wrap = new JPanel(new BorderLayout(0, 12));
        wrap.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        lblTonKho = cardValue("0 VNĐ"); lblTongTaiSan = cardValue("0");
        lblTongThietBi = cardValue("0"); lblYeuCauCho = cardValue("0");

        cards.add(card("📦 Giá trị tồn kho", lblTonKho, new Color(0x1565C0)));
        cards.add(card("🏷 Tài sản", lblTongTaiSan, new Color(0x6A1B9A)));
        cards.add(card("💻 Thiết bị", lblTongThietBi, new Color(0xEF6C00)));
        cards.add(card("📨 Yêu cầu chờ", lblYeuCauCho, new Color(0xC62828)));

        chartTrangThai = emptyChartPanel();
        JPanel chartWrap = new JPanel(new BorderLayout());
        chartWrap.setBorder(BorderFactory.createTitledBorder("Phân bố tài sản theo trạng thái"));
        chartWrap.add(chartTrangThai, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("↻ Làm mới");
        btnRefresh.setBackground(Theme.primary()); btnRefresh.setForeground(Theme.textOn());
        btnRefresh.addActionListener(e -> refreshAll());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.add(btnRefresh);

        wrap.add(cards, BorderLayout.NORTH);
        wrap.add(chartWrap, BorderLayout.CENTER);
        wrap.add(actions, BorderLayout.SOUTH);
        return wrap;
    }

    private JPanel buildTabTaiSan() {
        JPanel wrap = new JPanel(new GridLayout(1, 2, 12, 0));
        wrap.setBorder(new EmptyBorder(12, 12, 12, 12));
        chartLoai = emptyChartPanel(); chartYeuCau = emptyChartPanel();
        
        JPanel p1 = new JPanel(new BorderLayout()); p1.setBorder(BorderFactory.createTitledBorder("Số lượng theo loại tài sản")); p1.add(chartLoai);
        JPanel p2 = new JPanel(new BorderLayout()); p2.setBorder(BorderFactory.createTitledBorder("Trạng thái yêu cầu cấp phát")); p2.add(chartYeuCau);
        
        wrap.add(p1); wrap.add(p2);
        return wrap;
    }

    private JPanel buildTabNghiepVu() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(new EmptyBorder(12, 12, 12, 12));
        chartPhieu = emptyChartPanel();
        JPanel chartWrap = new JPanel(new BorderLayout()); chartWrap.setBorder(BorderFactory.createTitledBorder("Tổng số phiếu theo nghiệp vụ")); chartWrap.add(chartPhieu);
        wrap.add(chartWrap, BorderLayout.CENTER);
        return wrap;
    }

    private JLabel cardValue(String text) { JLabel l = new JLabel(text); l.setFont(new Font("Segoe UI", Font.BOLD, 22)); l.setForeground(Color.WHITE); return l; }

    private JPanel card(String title, JLabel val, Color bg) {
        JPanel p = new JPanel(new BorderLayout(4, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12); g2.dispose(); super.paintComponent(g);
            }
        };
        p.setOpaque(false); p.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel lbl = new JLabel(title); lbl.setForeground(new Color(0xE0E0E0));
        p.add(lbl, BorderLayout.NORTH); p.add(val, BorderLayout.CENTER);
        return p;
    }

    private ChartPanel emptyChartPanel() {
        ChartPanel cp = new ChartPanel(ChartFactory.createBarChart("", "", "", new DefaultCategoryDataset()));
        cp.setBackground(Color.WHITE); cp.setMouseWheelEnabled(true);
        return cp;
    }

    public void refreshAll() {
        try {
            lblTonKho.setText(MONEY_FMT.format(bus.tongGiaTriTonKho()) + " VNĐ");
            lblTongTaiSan.setText(String.valueOf(bus.taiSanTheoTrangThai().values().stream().mapToInt(Integer::intValue).sum()));
            lblTongThietBi.setText(String.valueOf(bus.thietBiTheoTrangThai().values().stream().mapToInt(Integer::intValue).sum()));
            Map<String, Integer> yc = bus.yeuCauTheoTrangThai();
            lblYeuCauCho.setText(String.valueOf(yc.getOrDefault("Chờ duyệt", 0)));

            chartTrangThai.setChart(createPieChart("Trạng thái tài sản", bus.taiSanTheoTrangThai()));
            chartLoai.setChart(createBarChart("Loại tài sản", "Loại", "SL", bus.taiSanTheoLoai()));
            chartYeuCau.setChart(createBarChart("Yêu cầu", "Trạng thái", "SL", yc));
            chartPhieu.setChart(createBarChart("Phiếu nghiệp vụ", "Loại phiếu", "Số lượng", bus.phieuNghiepVu()));
        } catch (Exception ex) { Dialogs.error(this, ex); }
    }

    private JFreeChart createBarChart(String title, String cat, String val, Map<String, Integer> data) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        data.forEach((k, v) -> ds.addValue(v, "Số lượng", k));
        JFreeChart chart = ChartFactory.createBarChart(title, cat, val, ds, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot(); plot.setBackgroundPaint(Color.WHITE);
        BarRenderer r = (BarRenderer) plot.getRenderer(); r.setSeriesPaint(0, new Color(0x1565C0)); r.setBarPainter(new StandardBarPainter());
        return chart;
    }

    private JFreeChart createPieChart(String title, Map<String, Integer> data) {
        DefaultPieDataset<String> ds = new DefaultPieDataset<>();
        data.forEach(ds::setValue);
        JFreeChart chart = ChartFactory.createPieChart(title, ds, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        PiePlot<?> plot = (PiePlot<?>) chart.getPlot(); plot.setBackgroundPaint(Color.WHITE);
        return chart;
    }
}